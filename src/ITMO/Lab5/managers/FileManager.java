package ITMO.Lab5.managers;

import ITMO.Lab5.exceptions.FileOperationException;
import ITMO.Lab5.exceptions.ValidationException;
import ITMO.Lab5.model.AstartesCategory;
import ITMO.Lab5.model.Chapter;
import ITMO.Lab5.model.Coordinates;
import ITMO.Lab5.model.MeleeWeapon;
import ITMO.Lab5.model.SpaceMarine;
import ITMO.Lab5.model.Weapon;
import ITMO.Lab5.util.EnumUtils;
import ITMO.Lab5.util.SimpleJson;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Handles loading and saving collection data in JSON format.
 */
public final class FileManager {
    private final Path filePath;
    private final PrintStream out;

    public FileManager(Path filePath, PrintStream out) {
        this.filePath = filePath.toAbsolutePath().normalize();
        this.out = out;
    }

    public Path getFilePath() {
        return filePath;
    }

    public List<SpaceMarine> load() throws FileOperationException {
        if (!Files.exists(filePath)) {
            return new ArrayList<SpaceMarine>();
        }

        if (!Files.isReadable(filePath)) {
            throw new FileOperationException("File is not readable: " + filePath);
        }

        String content = readFileContent();
        if (content.trim().isEmpty()) {
            return new ArrayList<SpaceMarine>();
        }

        Object root;
        try {
            root = SimpleJson.parse(content);
        } catch (ValidationException e) {
            throw new FileOperationException("Invalid JSON format in file: " + filePath, e);
        }

        if (!(root instanceof List<?>)) {
            throw new FileOperationException("JSON root must be an array");
        }

        List<?> rawList = (List<?>) root;
        List<SpaceMarine> loaded = new ArrayList<SpaceMarine>();
        Set<Integer> seenIds = new HashSet<Integer>();
        int skipped = 0;

        for (int i = 0; i < rawList.size(); i++) {
            Object raw = rawList.get(i);
            try {
                SpaceMarine marine = parseSpaceMarine(raw);
                if (!seenIds.add(marine.getId())) {
                    throw new ValidationException("Duplicate id: " + marine.getId());
                }
                loaded.add(marine);
            } catch (ValidationException e) {
                skipped++;
                out.println("Warning: skipped invalid record #" + (i + 1) + ": " + e.getMessage());
            }
        }

        if (skipped > 0) {
            out.println("Loaded valid records: " + loaded.size() + ", skipped: " + skipped);
        }

        return loaded;
    }

    public void save(List<SpaceMarine> marines) throws FileOperationException {
        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new FileOperationException("Cannot create directory for file: " + filePath, e);
            }
        }

        List<Map<String, Object>> rawList = new ArrayList<Map<String, Object>>();
        for (SpaceMarine marine : marines) {
            rawList.add(spaceMarineToMap(marine));
        }

        String json = SimpleJson.stringify(rawList);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(
                Files.newOutputStream(filePath, CREATE, WRITE, TRUNCATE_EXISTING)
        )) {
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (IOException e) {
            throw new FileOperationException("Failed to write file: " + filePath, e);
        }
    }

    private String readFileContent() throws FileOperationException {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];

        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(filePath), StandardCharsets.UTF_8)) {
            int count;
            while ((count = reader.read(buffer)) >= 0) {
                builder.append(buffer, 0, count);
            }
            return builder.toString();
        } catch (IOException e) {
            throw new FileOperationException("Failed to read file: " + filePath, e);
        }
    }

    private SpaceMarine parseSpaceMarine(Object rawObject) throws ValidationException {
        Map<String, Object> map = asObjectMap(rawObject, "record");

        int id = readRequiredInteger(map.get("id"), "id");
        String name = readRequiredString(map.get("name"), "name");

        Coordinates coordinates = parseCoordinates(map.get("coordinates"));

        String creationDateText = readRequiredString(map.get("creationDate"), "creationDate");
        LocalDate creationDate;
        try {
            creationDate = LocalDate.parse(creationDateText);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid creationDate format");
        }

        double health = readRequiredDouble(map.get("health"), "health");

        AstartesCategory category = parseEnum(map.get("category"), AstartesCategory.class, "category", true);
        Weapon weaponType = parseEnum(map.get("weaponType"), Weapon.class, "weaponType", true);
        MeleeWeapon meleeWeapon = parseEnum(map.get("meleeWeapon"), MeleeWeapon.class, "meleeWeapon", true);

        Chapter chapter = parseChapter(map.get("chapter"));

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

    private Coordinates parseCoordinates(Object rawObject) throws ValidationException {
        Map<String, Object> map = asObjectMap(rawObject, "coordinates");
        double x = readRequiredDouble(map.get("x"), "coordinates.x");
        float y = readRequiredFloat(map.get("y"), "coordinates.y");
        return new Coordinates(x, y);
    }

    private Chapter parseChapter(Object rawObject) throws ValidationException {
        Map<String, Object> map = asObjectMap(rawObject, "chapter");
        String name = readRequiredString(map.get("name"), "chapter.name");
        int marinesCount = readRequiredInteger(map.get("marinesCount"), "chapter.marinesCount");
        return new Chapter(name, marinesCount);
    }

    private <E extends Enum<E>> E parseEnum(
            Object rawValue,
            Class<E> enumClass,
            String fieldName,
            boolean nullable
    ) throws ValidationException {
        if (rawValue == null) {
            if (nullable) {
                return null;
            }
            throw new ValidationException(fieldName + " is required");
        }

        if (!(rawValue instanceof String)) {
            throw new ValidationException(fieldName + " must be a string");
        }

        return EnumUtils.parseEnum(enumClass, (String) rawValue, nullable);
    }

    private Integer readRequiredInteger(Object rawValue, String fieldName) throws ValidationException {
        Number number = readRequiredNumber(rawValue, fieldName);
        double value = number.doubleValue();
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new ValidationException(fieldName + " must be a finite number");
        }
        if (Math.rint(value) != value) {
            throw new ValidationException(fieldName + " must be an integer number");
        }
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new ValidationException(fieldName + " is out of integer range");
        }
        return (int) value;
    }

    private float readRequiredFloat(Object rawValue, String fieldName) throws ValidationException {
        Number number = readRequiredNumber(rawValue, fieldName);
        float value = number.floatValue();
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            throw new ValidationException(fieldName + " must be a finite number");
        }
        return value;
    }

    private double readRequiredDouble(Object rawValue, String fieldName) throws ValidationException {
        Number number = readRequiredNumber(rawValue, fieldName);
        double value = number.doubleValue();
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new ValidationException(fieldName + " must be a finite number");
        }
        return value;
    }

    private Number readRequiredNumber(Object rawValue, String fieldName) throws ValidationException {
        if (rawValue == null) {
            throw new ValidationException(fieldName + " is required");
        }
        if (!(rawValue instanceof Number)) {
            throw new ValidationException(fieldName + " must be a number");
        }
        return (Number) rawValue;
    }

    private String readRequiredString(Object rawValue, String fieldName) throws ValidationException {
        if (!(rawValue instanceof String)) {
            throw new ValidationException(fieldName + " must be a string");
        }
        String value = ((String) rawValue).trim();
        if (value.isEmpty()) {
            throw new ValidationException(fieldName + " must not be empty");
        }
        return value;
    }

    private Map<String, Object> asObjectMap(Object rawObject, String fieldName) throws ValidationException {
        if (!(rawObject instanceof Map<?, ?>)) {
            throw new ValidationException(fieldName + " must be a JSON object");
        }

        Map<?, ?> rawMap = (Map<?, ?>) rawObject;
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new ValidationException("JSON object key must be a string in " + fieldName);
            }
            result.put((String) entry.getKey(), entry.getValue());
        }
        return result;
    }

    private Map<String, Object> spaceMarineToMap(SpaceMarine marine) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", marine.getId());
        map.put("name", marine.getName());
        map.put("coordinates", coordinatesToMap(marine.getCoordinates()));
        map.put("creationDate", marine.getCreationDate().toString());
        map.put("health", marine.getHealth());
        map.put("category", marine.getCategory() == null ? null : marine.getCategory().name());
        map.put("weaponType", marine.getWeaponType() == null ? null : marine.getWeaponType().name());
        map.put("meleeWeapon", marine.getMeleeWeapon() == null ? null : marine.getMeleeWeapon().name());
        map.put("chapter", chapterToMap(marine.getChapter()));
        return map;
    }

    private Map<String, Object> coordinatesToMap(Coordinates coordinates) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("x", coordinates.getX());
        map.put("y", coordinates.getY());
        return map;
    }

    private Map<String, Object> chapterToMap(Chapter chapter) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("name", chapter.getName());
        map.put("marinesCount", chapter.getMarinesCount());
        return map;
    }
}
