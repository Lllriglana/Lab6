package ITMO.Lab5.util;

import ITMO.Lab5.exceptions.ValidationException;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utility methods for enum parsing and formatting.
 */
public final class EnumUtils {
    private EnumUtils() {
    }

    public static <E extends Enum<E>> E parseEnum(Class<E> enumClass, String raw, boolean nullable) throws ValidationException {
        if (raw == null) {
            if (nullable) {
                return null;
            }
            throw new ValidationException("Value is required");
        }

        String normalized = raw.trim();
        if (normalized.isEmpty()) {
            if (nullable) {
                return null;
            }
            throw new ValidationException("Value is required");
        }

        for (E constant : enumClass.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(normalized)) {
                return constant;
            }
        }

        throw new ValidationException("Invalid value. Allowed values: " + allowedValues(enumClass));
    }

    public static <E extends Enum<E>> String allowedValues(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}
