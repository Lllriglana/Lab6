package ITMO.Lab5.input;

import ITMO.Lab5.exceptions.InputException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages command/field input from console and script files.
 */
public final class InputManager implements AutoCloseable {
    private final BufferedReader consoleReader;
    private final PrintStream out;
    private final Deque<ScriptContext> scriptStack;
    private final Set<Path> activeScripts;

    public InputManager(InputStream inputStream, PrintStream out) {
        this.consoleReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        this.out = out;
        this.scriptStack = new ArrayDeque<ScriptContext>();
        this.activeScripts = new HashSet<Path>();
    }

    public boolean isInteractiveMode() {
        return scriptStack.isEmpty();
    }

    public String readCommandLine() throws InputException {
        while (true) {
            if (scriptStack.isEmpty()) {
                out.print("> ");
                out.flush();
                return readLine(consoleReader, "Failed to read command from console");
            }

            ScriptContext currentScript = scriptStack.peek();
            String line = readLine(currentScript.reader, "Failed to read command from script");
            if (line == null) {
                closeCurrentScript();
                continue;
            }
            return line;
        }
    }

    public String readFieldLine(String prompt) throws InputException {
        if (scriptStack.isEmpty()) {
            out.print(prompt + ": ");
            out.flush();
            String line = readLine(consoleReader, "Failed to read field from console");
            if (line == null) {
                throw new InputException("Input stream is closed");
            }
            return line;
        }

        ScriptContext currentScript = scriptStack.peek();
        String line = readLine(currentScript.reader, "Failed to read field from script");
        if (line == null) {
            closeCurrentScript();
            throw new InputException("Unexpected end of script while reading field: " + prompt);
        }
        return line;
    }

    public void pushScript(String rawPath) throws InputException {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            throw new InputException("Script file path is required");
        }

        Path scriptPath;
        try {
            scriptPath = Paths.get(rawPath.trim()).toAbsolutePath().normalize();
        } catch (InvalidPathException e) {
            throw new InputException("Invalid script path: " + rawPath, e);
        }

        if (activeScripts.contains(scriptPath)) {
            throw new InputException("Recursive script execution is not allowed: " + scriptPath);
        }
        if (!Files.exists(scriptPath)) {
            throw new InputException("Script file does not exist: " + scriptPath);
        }
        if (!Files.isRegularFile(scriptPath)) {
            throw new InputException("Script path is not a file: " + scriptPath);
        }
        if (!Files.isReadable(scriptPath)) {
            throw new InputException("Script file is not readable: " + scriptPath);
        }

        try {
            BufferedReader scriptReader = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(scriptPath), StandardCharsets.UTF_8)
            );
            scriptStack.push(new ScriptContext(scriptPath, scriptReader));
            activeScripts.add(scriptPath);
        } catch (IOException e) {
            throw new InputException("Cannot open script file: " + scriptPath, e);
        }
    }

    @Override
    public void close() {
        while (!scriptStack.isEmpty()) {
            closeCurrentScript();
        }
        try {
            consoleReader.close();
        } catch (IOException ignored) {
            // Ignore close failure during shutdown.
        }
    }

    private String readLine(BufferedReader reader, String errorMessage) throws InputException {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new InputException(errorMessage, e);
        }
    }

    private void closeCurrentScript() {
        ScriptContext context = scriptStack.pop();
        activeScripts.remove(context.path);
        try {
            context.reader.close();
        } catch (IOException ignored) {
            // Ignore close failure during script cleanup.
        }
    }

    /**
     * Holds state for a script being executed from the stack.
     */
    private static final class ScriptContext {
        private final Path path;
        private final BufferedReader reader;

        private ScriptContext(Path path, BufferedReader reader) {
            this.path = path;
            this.reader = reader;
        }
    }
}
