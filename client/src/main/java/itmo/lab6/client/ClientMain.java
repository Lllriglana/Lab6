package itmo.lab6.client;

import itmo.lab6.input.*;
import itmo.lab6.model.Weapon;
import itmo.lab6.protocol.*;
import itmo.lab6.exceptions.*;
import itmo.lab6.util.*;
import java.io.IOException;
import java.util.*;

/** Console/script frontend; owns neither the collection nor its persistence file. */
public final class ClientMain {
    private ClientMain() { }
    public static void main(String[] args) {
        if (args.length > 2) {
            System.err.println("Usage: java -jar Lab6-client.jar [host=localhost] [port=22226]");
            System.exit(1);
        }
        try {
            int port = args.length == 2 ? Integer.parseInt(args[1]) : 22226;
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Port must be 1..65535");
            try (UdpClient client = new UdpClient(args.length > 0 ? args[0] : "localhost", port);
                 InputManager input = new InputManager(System.in, System.out)) {
                run(client, input);
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Client error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run(UdpClient client, InputManager input) {
        SpaceMarineInputReader fields = new SpaceMarineInputReader(input, System.out);
        Deque<String> history = new ArrayDeque<>();
        Request pending = null;
        System.out.println("Lab6 UDP client. Type help. Scripts run locally; exit closes only this client.");
        while (true) {
            try {
                String line = input.readCommandLine();
                if (line == null) return;
                var parsed = CommandLineParser.parse(line);
                String name = parsed.getName().toLowerCase(Locale.ROOT), arg = parsed.getArgument();
                if (name.isEmpty() || name.startsWith("#")) continue;
                if (name.equals("exit")) {
                    noArgument(arg); System.out.println("Client terminated."); return;
                }
                if (name.equals("save")) {
                    System.out.println("save is available only in the server console."); continue;
                }
                if (name.equals("history")) {
                    noArgument(arg); remember(history, name); history.forEach(System.out::println); continue;
                }
                if (pending != null && !name.equals("retry")) {
                    System.out.println("A request has no confirmed response. Use retry or exit; scripts have been stopped.");
                    continue;
                }
                if (name.equals("execute_script")) {
                    if (arg.isEmpty()) throw new ValidationException("Script path is required");
                    input.pushScript(arg); remember(history, name); continue;
                }
                Request request;
                if (name.equals("retry")) {
                    noArgument(arg);
                    if (pending == null) { System.out.println("No pending request."); continue; }
                    request = pending;
                } else {
                    CommandType command;
                    try { command = CommandType.valueOf(name.toUpperCase(Locale.ROOT)); }
                    catch (IllegalArgumentException e) { throw new ValidationException("Unknown command: " + name); }
                    Integer id = null;
                    Weapon weapon = null;
                    MarineData element = null;
                    switch (command) {
                        case UPDATE, REMOVE_BY_ID -> {
                            try { id = Validators.requireValidId(Integer.parseInt(arg)); }
                            catch (NumberFormatException e) { throw new ValidationException("id must be a positive int"); }
                        }
                        case FILTER_BY_WEAPON_TYPE, COUNT_LESS_THAN_WEAPON_TYPE -> {
                            if (arg.isEmpty()) throw new ValidationException("Weapon required: MELTAGUN, COMBI_FLAMER, GRENADE_LAUNCHER or null");
                            weapon = arg.equalsIgnoreCase("null") ? null : EnumUtils.parseEnum(Weapon.class, arg, false);
                        }
                        default -> noArgument(arg);
                    }
                    if (command == CommandType.ADD || command == CommandType.ADD_IF_MIN || command == CommandType.UPDATE)
                        element = fields.readSpaceMarine();
                    request = new Request(UUID.randomUUID(), command, id, weapon, element);
                    remember(history, name);
                }
                pending = request;
                try {
                    Response response = client.send(request);
                    pending = null;
                    if (!response.message().isEmpty()) System.out.println((response.success() ? "" : "Command error: ") + response.message());
                    response.elements().forEach(System.out::println);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    input.cancelScripts();
                }
            } catch (InputException | ValidationException e) {
                System.out.println("Input error: " + e.getMessage());
            }
        }
    }

    private static void noArgument(String argument) throws ValidationException {
        if (!argument.isEmpty()) throw new ValidationException("This command accepts no arguments");
    }
    private static void remember(Deque<String> history, String command) {
        history.addLast(command);
        if (history.size() > 11) history.removeFirst();
    }
}
