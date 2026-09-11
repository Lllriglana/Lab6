package ITMO.Lab5.model;

import ITMO.Lab5.exceptions.ValidationException;
import ITMO.Lab5.util.Validators;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain object managed by the collection.
 */
public final class SpaceMarine implements Comparable<SpaceMarine> {
    private final int id;
    private final String name;
    private final Coordinates coordinates;
    private final LocalDate creationDate;
    private final double health;
    private final AstartesCategory category;
    private final Weapon weaponType;
    private final MeleeWeapon meleeWeapon;
    private final Chapter chapter;

    public SpaceMarine(
            int id,
            String name,
            Coordinates coordinates,
            LocalDate creationDate,
            double health,
            AstartesCategory category,
            Weapon weaponType,
            MeleeWeapon meleeWeapon,
            Chapter chapter
    ) throws ValidationException {
        this.id = Validators.requireValidId(id);
        this.name = Validators.requireNotBlank(name, "name");
        this.coordinates = Validators.requireNotNull(coordinates, "coordinates");
        this.creationDate = Validators.requireNotNull(creationDate, "creationDate");
        this.health = Validators.requireGreaterThanZero(health, "health");
        this.category = category;
        this.weaponType = weaponType;
        this.meleeWeapon = meleeWeapon;
        this.chapter = Validators.requireNotNull(chapter, "chapter");
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public double getHealth() {
        return health;
    }

    public AstartesCategory getCategory() {
        return category;
    }

    public Weapon getWeaponType() {
        return weaponType;
    }

    public MeleeWeapon getMeleeWeapon() {
        return meleeWeapon;
    }

    public Chapter getChapter() {
        return chapter;
    }

    @Override
    public int compareTo(SpaceMarine other) {
        int byHealth = Double.compare(this.health, other.health);
        if (byHealth != 0) {
            return byHealth;
        }
        return Integer.compare(this.id, other.id);
    }

    @Override
    public String toString() {
        return "SpaceMarine{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", health=" + health +
                ", category=" + category +
                ", weaponType=" + weaponType +
                ", meleeWeapon=" + meleeWeapon +
                ", chapter=" + chapter +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpaceMarine that = (SpaceMarine) o;
        return Double.compare(that.health, health) == 0
            && Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(coordinates, that.coordinates)
                && Objects.equals(creationDate, that.creationDate)
                && category == that.category
                && weaponType == that.weaponType
                && meleeWeapon == that.meleeWeapon
                && Objects.equals(chapter, that.chapter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, coordinates, creationDate, health, category, weaponType, meleeWeapon, chapter);
    }
}
