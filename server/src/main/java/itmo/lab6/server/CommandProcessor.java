package itmo.lab6.server;

import itmo.lab6.protocol.*;
import itmo.lab6.model.SpaceMarine;
import itmo.lab6.exceptions.ValidationException;

/** Executes validated network commands. File persistence is never exposed here. */
public final class CommandProcessor {
    private final CollectionManager collection;
    public CommandProcessor(CollectionManager collection) { this.collection = collection; }

    public Response execute(Request request) {
        try {
            validate(request);
            String message;
            switch (request.command()) {
                case HELP -> message = "help, info, show, add, update <id>, remove_by_id <id>, clear,\n"
                        + "add_if_min, shuffle, count_less_than_weapon_type <weapon>,\n"
                        + "filter_by_weapon_type <weapon>, print_unique_category\n"
                        + "Client commands: history, execute_script <file>, retry, exit\n"
                        + "Weapons: MELTAGUN, COMBI_FLAMER, GRENADE_LAUNCHER (null allowed)";
                case INFO -> message = collection.info();
                case SHOW -> { return Response.collection(request.id(), collection.all()); }
                case FILTER_BY_WEAPON_TYPE -> { return Response.collection(request.id(), collection.filter(request.weapon())); }
                case COUNT_LESS_THAN_WEAPON_TYPE -> message = Long.toString(collection.countLess(request.weapon()));
                case PRINT_UNIQUE_CATEGORY -> {
                    message = collection.categories();
                    if (message.isEmpty()) message = "Collection is empty.";
                }
                case CLEAR -> { collection.clear(); message = "Collection was cleared."; }
                case SHUFFLE -> { collection.shuffle(); message = "Collection was shuffled. Responses are sorted by default."; }
                case REMOVE_BY_ID -> message = collection.remove(request.elementId()) ? "Element removed."
                        : "Element with id=" + request.elementId() + " was not found.";
                case UPDATE -> message = collection.update(request.elementId(), request.element()) ? "Element updated."
                        : "Element with id=" + request.elementId() + " was not found.";
                case ADD, ADD_IF_MIN -> {
                    SpaceMarine added = collection.add(request.element(), request.command() == CommandType.ADD_IF_MIN);
                    message = added == null ? "Element was not added: it is not less than the minimum."
                            : "Element added: id=" + added.getId();
                }
                default -> throw new ValidationException("Unsupported command");
            }
            return Response.message(request.id(), true, message);
        } catch (ValidationException | IllegalArgumentException e) {
            return Response.message(request.id(), false, e.getMessage());
        }
    }

    private void validate(Request r) throws ValidationException {
        if (r.id() == null || r.command() == null) throw new ValidationException("Missing request id or command");
        boolean idRequired = r.command() == CommandType.UPDATE || r.command() == CommandType.REMOVE_BY_ID;
        boolean objectRequired = r.command() == CommandType.UPDATE || r.command() == CommandType.ADD
                || r.command() == CommandType.ADD_IF_MIN;
        boolean weaponAllowed = r.command() == CommandType.FILTER_BY_WEAPON_TYPE
                || r.command() == CommandType.COUNT_LESS_THAN_WEAPON_TYPE;
        if (idRequired != (r.elementId() != null) || r.elementId() != null && r.elementId() <= 0)
            throw new ValidationException("Invalid id argument");
        if (objectRequired != (r.element() != null)) throw new ValidationException("Invalid element argument");
        if (!weaponAllowed && r.weapon() != null) throw new ValidationException("Unexpected weapon argument");
        if (r.element() != null) r.element().create(1, java.time.LocalDate.of(1970, 1, 1));
    }
}
