package ITMO.Lab5.util;

/**
 * Parses raw command line input into command name and optional argument.
 */
public final class CommandLineParser {
    private CommandLineParser() {
    }

    public static ParsedCommand parse(String line) {
        if (line == null) {
            return new ParsedCommand("", "");
        }

        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return new ParsedCommand("", "");
        }

        int firstSpace = trimmed.indexOf(' ');
        if (firstSpace < 0) {
            return new ParsedCommand(trimmed, "");
        }

        String name = trimmed.substring(0, firstSpace).trim();
        String argument = trimmed.substring(firstSpace + 1).trim();
        return new ParsedCommand(name, argument);
    }

    /**
     * Parsed command line container.
     */
    public static final class ParsedCommand {
        private final String name;
        private final String argument;

        public ParsedCommand(String name, String argument) {
            this.name = name;
            this.argument = argument;
        }

        public String getName() {
            return name;
        }

        public String getArgument() {
            return argument;
        }
    }
}
