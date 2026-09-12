package itmo.lab6.util;

import itmo.lab6.exceptions.ValidationException;

/**
 * Utility class with validation helpers for domain constraints.
 */
public final class Validators {
    private Validators() {
    }

    public static int requireValidId(int id) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException("id must be greater than 0");
        }
        return id;
    }

    public static String requireNotBlank(String value, String fieldName) throws ValidationException {
        if (value == null) {
            throw new ValidationException(fieldName + " must not be null");
        }
        if (value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " must not be empty");
        }
        return value;
    }

    public static <T> T requireNotNull(T value, String fieldName) throws ValidationException {
        if (value == null) {
            throw new ValidationException(fieldName + " must not be null");
        }
        return value;
    }

    public static int requireGreaterThanZero(int value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be greater than 0");
        }
        return value;
    }

    public static double requireGreaterThanZero(double value, String fieldName) throws ValidationException {
        if (!Double.isFinite(value) || value <= 0) {
            throw new ValidationException(fieldName + " must be a finite number greater than 0");
        }
        return value;
    }

    public static long requireLessOrEqual(long value, long max, String fieldName) throws ValidationException {
        if (value > max) {
            throw new ValidationException(fieldName + " must be less than or equal to " + max);
        }
        return value;
    }

    public static float requireLessOrEqual(float value, float max, String fieldName) throws ValidationException {
        if (value > max) {
            throw new ValidationException(fieldName + " must be less than or equal to " + max);
        }
        return value;
    }

    /** Validates that a floating-point value is finite and strictly above a bound. */
    public static double requireGreaterThan(Double value, double min, String fieldName) throws ValidationException {
        if (value == null || !Double.isFinite(value) || value <= min) {
            throw new ValidationException(fieldName + " must be greater than " + min);
        }
        return value;
    }

    /** Validates an inclusive integer range. */
    public static int requireRange(int value, int min, int max, String fieldName) throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(fieldName + " must be between " + min + " and " + max);
        }
        return value;
    }
}
