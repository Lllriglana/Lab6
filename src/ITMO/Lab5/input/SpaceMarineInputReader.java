package ITMO.Lab5.input;

import ITMO.Lab5.exceptions.InputException;
import ITMO.Lab5.exceptions.ValidationException;
import ITMO.Lab5.model.AstartesCategory;
import ITMO.Lab5.model.Chapter;
import ITMO.Lab5.model.Coordinates;
import ITMO.Lab5.model.MeleeWeapon;
import ITMO.Lab5.model.SpaceMarine;
import ITMO.Lab5.model.Weapon;
import ITMO.Lab5.util.EnumUtils;
import ITMO.Lab5.util.Validators;

import java.io.PrintStream;
import java.time.LocalDate;

/**
 * Reads and validates composite SpaceMarine input from current input source.
 */
public final class SpaceMarineInputReader {
    private final InputManager inputManager;
    private final PrintStream out;

    public SpaceMarineInputReader(InputManager inputManager, PrintStream out) {
        this.inputManager = inputManager;
        this.out = out;
    }

    public SpaceMarine readSpaceMarine(int id, LocalDate creationDate) throws InputException, ValidationException {
        String name = readRequiredString("name (required)", "name");

        double x = readDoubleGreaterThan("coordinates.x (double, > -540)", -540.0, "coordinates.x");
        float y = readFloatRequired("coordinates.y (float, required)", "coordinates.y");

        double health = readPositiveDouble("health (double, > 0)", "health");

        String categoryPrompt = "category (" + EnumUtils.allowedValues(AstartesCategory.class) + ", empty for null)";
        AstartesCategory category = readEnum(categoryPrompt, AstartesCategory.class, true);

        String weaponPrompt = "weaponType (" + EnumUtils.allowedValues(Weapon.class) + ", empty for null)";
        Weapon weaponType = readEnum(weaponPrompt, Weapon.class, true);

        String meleePrompt = "meleeWeapon (" + EnumUtils.allowedValues(MeleeWeapon.class) + ", empty for null)";
        MeleeWeapon meleeWeapon = readEnum(meleePrompt, MeleeWeapon.class, true);

        String chapterName = readRequiredString("chapter.name (required)", "chapter.name");
        int marinesCount = readIntegerInRange("chapter.marinesCount (int, 1..1000)", 1, 1000, "chapter.marinesCount");

        Coordinates coordinates = new Coordinates(x, y);
        Chapter chapter = new Chapter(chapterName, marinesCount);

        return new SpaceMarine(
                id,
                name,
                coordinates,
                creationDate,
                health,
                category,
                weaponType,
                meleeWeapon,
                chapter
        );
    }

    private String readRequiredString(String prompt, String fieldName) throws InputException {
        return readUntilValid(prompt, raw -> Validators.requireNotBlank(raw == null ? null : raw.trim(), fieldName));
    }

    private double readDoubleGreaterThan(String prompt, double min, String fieldName) throws InputException {
        return readUntilValid(prompt, raw -> {
            String trimmed = normalizeRequiredNumeric(raw, fieldName);
            double value;
            try {
                value = Double.parseDouble(trimmed);
            } catch (NumberFormatException e) {
                throw new ValidationException(fieldName + " must be a double number");
            }
            return Validators.requireGreaterThan(value, min, fieldName);
        });
    }

    private float readFloatRequired(String prompt, String fieldName) throws InputException {
        return readUntilValid(prompt, raw -> {
            String trimmed = normalizeRequiredNumeric(raw, fieldName);
            try {
                float value = Float.parseFloat(trimmed);
                if (!Float.isFinite(value)) throw new NumberFormatException();
                return value;
            } catch (NumberFormatException e) {
                throw new ValidationException(fieldName + " must be a finite float number");
            }
        });
    }

    private int readIntegerInRange(String prompt, int min, int max, String fieldName) throws InputException {
        return readUntilValid(prompt, raw -> {
            try {
                return Validators.requireRange(Integer.parseInt(normalizeRequiredNumeric(raw, fieldName)), min, max, fieldName);
            } catch (NumberFormatException e) {
                throw new ValidationException(fieldName + " must be an integer number");
            }
        });
    }

    private double readPositiveDouble(String prompt, String fieldName) throws InputException {
        return readUntilValid(prompt, raw -> {
            String trimmed = normalizeRequiredNumeric(raw, fieldName);
            double value;
            try {
                value = Double.parseDouble(trimmed);
            } catch (NumberFormatException e) {
                throw new ValidationException(fieldName + " must be a double number");
            }
            Validators.requireGreaterThanZero(value, fieldName);
            return value;
        });
    }

    private <E extends Enum<E>> E readEnum(String prompt, Class<E> enumClass, boolean nullable) throws InputException {
        return readUntilValid(prompt, raw -> EnumUtils.parseEnum(enumClass, raw, nullable));
    }

    private String normalizeRequiredNumeric(String raw, String fieldName) throws ValidationException {
        if (raw == null || raw.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required");
        }
        return raw.trim();
    }

    private <T> T readUntilValid(String prompt, ValueParser<T> parser) throws InputException {
        while (true) {
            String raw = inputManager.readFieldLine(prompt);
            try {
                return parser.parse(raw);
            } catch (ValidationException e) {
                out.println("Input error: " + e.getMessage());
            }
        }
    }

    @FunctionalInterface
    private interface ValueParser<T> {
        T parse(String raw) throws ValidationException;
    }
}
