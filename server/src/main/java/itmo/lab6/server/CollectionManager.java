package itmo.lab6.server;

import itmo.lab6.model.*;
import itmo.lab6.protocol.MarineData;
import itmo.lab6.exceptions.ValidationException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/** Collection state owned by the single server event loop. */
public final class CollectionManager {
    private LinkedList<SpaceMarine> marines = new LinkedList<>();
    private final ZonedDateTime initialized = ZonedDateTime.now();
    private long nextId = 1;

    public CollectionManager(List<SpaceMarine> loaded) throws ValidationException {
        if (loaded.stream().map(SpaceMarine::getId).distinct().count() != loaded.size())
            throw new ValidationException("Duplicate ids in collection");
        marines.addAll(loaded);
        nextId = loaded.stream().mapToLong(SpaceMarine::getId).max().orElse(0) + 1;
    }

    public SpaceMarine add(MarineData data, boolean onlyMinimum) throws ValidationException {
        if (nextId > Integer.MAX_VALUE) throw new ValidationException("No more int ids available");
        SpaceMarine value = data.create((int) nextId, LocalDate.now());
        if (onlyMinimum && marines.stream().min(SpaceMarine::compareTo)
                .map(min -> value.compareTo(min) >= 0).orElse(false)) return null;
        marines.add(value);
        nextId++;
        return value;
    }

    public boolean update(int id, MarineData data) throws ValidationException {
        SpaceMarine old = marines.stream().filter(m -> m.getId() == id).findFirst().orElse(null);
        if (old == null) return false;
        SpaceMarine updated = data.create(id, old.getCreationDate());
        marines = marines.stream().map(m -> m.getId() == id ? updated : m)
                .collect(Collectors.toCollection(LinkedList::new));
        return true;
    }

    public boolean remove(int id) {
        int before = marines.size();
        marines = marines.stream().filter(m -> m.getId() != id).collect(Collectors.toCollection(LinkedList::new));
        return before != marines.size();
    }

    public void clear() { marines.clear(); }
    public void shuffle() { Collections.shuffle(marines); }
    public List<SpaceMarine> all() { return marines.stream().sorted().toList(); }
    public List<SpaceMarine> filter(Weapon weapon) {
        return marines.stream().filter(m -> m.getWeaponType() == weapon).sorted().toList();
    }
    public long countLess(Weapon weapon) {
        return marines.stream().filter(m -> weapon != null && m.getWeaponType() != null)
                .filter(m -> m.getWeaponType().compareTo(weapon) < 0).count();
    }
    public String categories() {
        return marines.stream().map(SpaceMarine::getCategory).distinct()
                .sorted(Comparator.nullsFirst(Comparator.naturalOrder())).map(String::valueOf)
                .collect(Collectors.joining("\n"));
    }
    public String info() {
        return "Collection type: java.util.LinkedList\nInitialization date: " + initialized
                + "\nElements count: " + marines.size();
    }
}
