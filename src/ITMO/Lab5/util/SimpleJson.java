package ITMO.Lab5.util;

import ITMO.Lab5.exceptions.ValidationException;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON parser and serializer for primitive JSON structures.
 */
public final class SimpleJson {
    private static final String INDENT = "  ";

    private SimpleJson() {
    }

    public static Object parse(String json) throws ValidationException {
        if (json == null) {
            throw new ValidationException("JSON text must not be null");
        }

        Parser parser = new Parser(json);
        Object value = parser.parseValue();
        parser.skipWhitespace();
        if (!parser.isEnd()) {
            throw parser.error("Unexpected trailing characters");
        }
        return value;
    }

    public static String stringify(Object value) {
        StringBuilder builder = new StringBuilder();
        writeValue(builder, value, 0);
        return builder.toString();
    }

    private static void writeValue(StringBuilder builder, Object value, int level) {
        if (value == null) {
            builder.append("null");
            return;
        }

        if (value instanceof String) {
            writeString(builder, (String) value);
            return;
        }

        if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
            return;
        }

        if (value instanceof Map<?, ?>) {
            writeObject(builder, (Map<?, ?>) value, level);
            return;
        }

        if (value instanceof List<?>) {
            writeArray(builder, (List<?>) value, level);
            return;
        }

        throw new IllegalArgumentException("Unsupported JSON value: " + value.getClass().getName());
    }

    private static void writeObject(StringBuilder builder, Map<?, ?> map, int level) {
        if (map.isEmpty()) {
            builder.append("{}");
            return;
        }

        builder.append("{\n");
        Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<?, ?> entry = iterator.next();
            appendIndent(builder, level + 1);
            writeString(builder, String.valueOf(entry.getKey()));
            builder.append(": ");
            writeValue(builder, entry.getValue(), level + 1);
            if (iterator.hasNext()) {
                builder.append(',');
            }
            builder.append('\n');
        }
        appendIndent(builder, level);
        builder.append('}');
    }

    private static void writeArray(StringBuilder builder, List<?> list, int level) {
        if (list.isEmpty()) {
            builder.append("[]");
            return;
        }

        builder.append("[\n");
        for (int i = 0; i < list.size(); i++) {
            appendIndent(builder, level + 1);
            writeValue(builder, list.get(i), level + 1);
            if (i + 1 < list.size()) {
                builder.append(',');
            }
            builder.append('\n');
        }
        appendIndent(builder, level);
        builder.append(']');
    }

    private static void writeString(StringBuilder builder, String text) {
        builder.append('"');
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        String hex = Integer.toHexString(ch);
                        builder.append("\\u");
                        for (int pad = hex.length(); pad < 4; pad++) {
                            builder.append('0');
                        }
                        builder.append(hex);
                    } else {
                        builder.append(ch);
                    }
            }
        }
        builder.append('"');
    }

    private static void appendIndent(StringBuilder builder, int level) {
        for (int i = 0; i < level; i++) {
            builder.append(INDENT);
        }
    }

    /**
     * Internal parser for minimal JSON structures.
     */
    private static final class Parser {
        private final String text;
        private int index;

        private Parser(String text) {
            this.text = text;
            this.index = 0;
        }

        private ValidationException error(String message) {
            return new ValidationException(message + " at position " + index);
        }

        private boolean isEnd() {
            return index >= text.length();
        }

        private void skipWhitespace() {
            while (!isEnd()) {
                char ch = text.charAt(index);
                if (!Character.isWhitespace(ch)) {
                    break;
                }
                index++;
            }
        }

        private Object parseValue() throws ValidationException {
            skipWhitespace();
            if (isEnd()) {
                throw error("Unexpected end of JSON");
            }

            char ch = text.charAt(index);
            switch (ch) {
                case '{':
                    return parseObject();
                case '[':
                    return parseArray();
                case '"':
                    return parseString();
                case 't':
                    consumeLiteral("true");
                    return Boolean.TRUE;
                case 'f':
                    consumeLiteral("false");
                    return Boolean.FALSE;
                case 'n':
                    consumeLiteral("null");
                    return null;
                default:
                    if (ch == '-' || Character.isDigit(ch)) {
                        return parseNumber();
                    }
                    throw error("Unexpected character '" + ch + "'");
            }
        }

        private Map<String, Object> parseObject() throws ValidationException {
            consume('{');
            skipWhitespace();

            Map<String, Object> map = new LinkedHashMap<String, Object>();
            if (tryConsume('}')) {
                return map;
            }

            while (true) {
                skipWhitespace();
                if (isEnd() || text.charAt(index) != '"') {
                    throw error("Object key must be a string");
                }

                String key = parseString();
                skipWhitespace();
                consume(':');
                Object value = parseValue();
                map.put(key, value);

                skipWhitespace();
                if (tryConsume('}')) {
                    break;
                }
                consume(',');
            }

            return map;
        }

        private List<Object> parseArray() throws ValidationException {
            consume('[');
            skipWhitespace();

            java.util.ArrayList<Object> list = new java.util.ArrayList<Object>();
            if (tryConsume(']')) {
                return list;
            }

            while (true) {
                list.add(parseValue());
                skipWhitespace();
                if (tryConsume(']')) {
                    break;
                }
                consume(',');
            }

            return list;
        }

        private String parseString() throws ValidationException {
            consume('"');
            StringBuilder builder = new StringBuilder();

            while (!isEnd()) {
                char ch = text.charAt(index++);
                if (ch == '"') {
                    return builder.toString();
                }

                if (ch == '\\') {
                    if (isEnd()) {
                        throw error("Invalid string escape");
                    }
                    char escaped = text.charAt(index++);
                    switch (escaped) {
                        case '"':
                        case '\\':
                        case '/':
                            builder.append(escaped);
                            break;
                        case 'b':
                            builder.append('\b');
                            break;
                        case 'f':
                            builder.append('\f');
                            break;
                        case 'n':
                            builder.append('\n');
                            break;
                        case 'r':
                            builder.append('\r');
                            break;
                        case 't':
                            builder.append('\t');
                            break;
                        case 'u':
                            builder.append(parseUnicodeEscape());
                            break;
                        default:
                            throw error("Unsupported escape sequence");
                    }
                } else {
                    if (ch < 0x20) {
                        throw error("Control character in string");
                    }
                    builder.append(ch);
                }
            }

            throw error("Unterminated string");
        }

        private char parseUnicodeEscape() throws ValidationException {
            if (index + 4 > text.length()) {
                throw error("Invalid unicode escape");
            }

            String hex = text.substring(index, index + 4);
            index += 4;
            try {
                return (char) Integer.parseInt(hex, 16);
            } catch (NumberFormatException e) {
                throw error("Invalid unicode escape");
            }
        }

        private Number parseNumber() throws ValidationException {
            int start = index;

            if (text.charAt(index) == '-') {
                index++;
            }

            if (isEnd()) {
                throw error("Invalid number");
            }

            if (text.charAt(index) == '0') {
                index++;
            } else {
                if (!Character.isDigit(text.charAt(index))) {
                    throw error("Invalid number");
                }
                while (!isEnd() && Character.isDigit(text.charAt(index))) {
                    index++;
                }
            }

            boolean floating = false;
            if (!isEnd() && text.charAt(index) == '.') {
                floating = true;
                index++;
                if (isEnd() || !Character.isDigit(text.charAt(index))) {
                    throw error("Invalid number");
                }
                while (!isEnd() && Character.isDigit(text.charAt(index))) {
                    index++;
                }
            }

            if (!isEnd()) {
                char ch = text.charAt(index);
                if (ch == 'e' || ch == 'E') {
                    floating = true;
                    index++;
                    if (!isEnd()) {
                        char sign = text.charAt(index);
                        if (sign == '+' || sign == '-') {
                            index++;
                        }
                    }
                    if (isEnd() || !Character.isDigit(text.charAt(index))) {
                        throw error("Invalid number");
                    }
                    while (!isEnd() && Character.isDigit(text.charAt(index))) {
                        index++;
                    }
                }
            }

            String token = text.substring(start, index);
            try {
                if (floating) {
                    return Double.parseDouble(token);
                }
                return Long.parseLong(token);
            } catch (NumberFormatException e) {
                throw error("Invalid number");
            }
        }

        private void consumeLiteral(String literal) throws ValidationException {
            if (index + literal.length() > text.length()) {
                throw error("Unexpected end of JSON");
            }

            String actual = text.substring(index, index + literal.length());
            if (!literal.equals(actual)) {
                throw error("Unexpected token");
            }
            index += literal.length();
        }

        private boolean tryConsume(char expected) {
            if (!isEnd() && text.charAt(index) == expected) {
                index++;
                return true;
            }
            return false;
        }

        private void consume(char expected) throws ValidationException {
            if (isEnd() || text.charAt(index) != expected) {
                throw error("Expected '" + expected + "'");
            }
            index++;
        }
    }
}
