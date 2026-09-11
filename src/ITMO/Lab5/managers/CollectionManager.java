package ITMO.Lab5.managers;

import ITMO.Lab5.exceptions.ValidationException;
import ITMO.Lab5.model.AstartesCategory;
import ITMO.Lab5.model.SpaceMarine;
import ITMO.Lab5.model.Weapon;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Manages in-memory collection of SpaceMarine objects.
 */
public final class CollectionManager {
    private final LinkedList<SpaceMarine> marines;
    private final ZonedDateTime initializationDate;
    private int nextId;

    public CollectionManager() {
        this.marines = new LinkedList<SpaceMarine>();
        this.initializationDate = ZonedDateTime.now();
        this.nextId = 1;
    }

    public synchronized void replaceAll(List<SpaceMarine> loadedMarines) throws ValidationException {
        ensureUniqueIds(loadedMarines);
        marines.clear();
        marines.addAll(loadedMarines);
        Collections.sort(marines);
        recalculateNextId();
    }

    public synchronized int generateNextId() {
        while (containsId(nextId)) {
            nextId++;
        }
        int generated = nextId;
        nextId++;
        return generated;
    }

    public synchronized void add(SpaceMarine marine) {
        marines.add(marine);
        Collections.sort(marines);
    }

    public synchronized boolean addIfMin(SpaceMarine marine) {
        if (marines.isEmpty()) {
            marines.add(marine);
            return true;
        }

        SpaceMarine min = Collections.min(marines);
        if (marine.compareTo(min) < 0) {
            marines.add(marine);
            Collections.sort(marines);
            return true;
        }
        return false;
    }

    public synchronized boolean updateById(int id, SpaceMarine updatedMarine) {
        for (int i = 0; i < marines.size(); i++) {
            if (marines.get(i).getId() == id) {
                marines.set(i, updatedMarine);
                Collections.sort(marines);
                return true;
            }
        }
        return false;
    }

    public synchronized SpaceMarine findById(int id) {
        for (SpaceMarine marine : marines) {
            if (marine.getId() == id) {
                return marine;
            }
        }
        return null;
    }

    public synchronized boolean removeById(int id) {
        return marines.removeIf(marine -> marine.getId() == id);
    }

    public synchronized void clear() {
        marines.clear();
    }

    public synchronized void shuffle() {
        Collections.shuffle(marines);
    }

    public synchronized long countLessThanWeaponType(Weapon weaponType) {
        if (weaponType == null) {
            return 0;
        }

        long count = 0;
        for (SpaceMarine marine : marines) {
            Weapon marineWeapon = marine.getWeaponType();
            if (marineWeapon != null && marineWeapon.compareTo(weaponType) < 0) {
                count++;
            }
        }
        return count;
    }

    public synchronized List<SpaceMarine> filterByWeaponType(Weapon weaponType) {
        List<SpaceMarine> result = new ArrayList<SpaceMarine>();
        for (SpaceMarine marine : marines) {
            if (marine.getWeaponType() == weaponType) {
                result.add(marine);
            }
        }
        return result;
    }

    public synchronized Set<AstartesCategory> getUniqueCategories() {
        Set<AstartesCategory> categories = new LinkedHashSet<AstartesCategory>();
        for (SpaceMarine marine : marines) {
            categories.add(marine.getCategory());
        }
        return categories;
    }

    public synchronized List<SpaceMarine> getAll() {
        return new ArrayList<SpaceMarine>(marines);
    }

    public synchronized int size() {
        return marines.size();
    }

    public synchronized String getCollectionType() {
        return marines.getClass().getName();
    }

    public ZonedDateTime getInitializationDate() {
        return initializationDate;
    }

    private void recalculateNextId() {
        int maxId = 0;
        for (SpaceMarine marine : marines) {
            if (marine.getId() > maxId) {
                maxId = marine.getId();
            }
        }
        nextId = maxId + 1;
    }

    private boolean containsId(int id) {
        for (SpaceMarine marine : marines) {
            if (marine.getId() == id) {
                return true;
            }
        }
        return false;
    }

    private void ensureUniqueIds(List<SpaceMarine> marinesToCheck) throws ValidationException {
        Set<Integer> ids = new HashSet<Integer>();
        for (SpaceMarine marine : marinesToCheck) {
            if (!ids.add(marine.getId())) {
                throw new ValidationException("Duplicate id in loaded data: " + marine.getId());
            }
        }
    }
}
