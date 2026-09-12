package itmo.lab6.protocol;

import itmo.lab6.model.*;
import itmo.lab6.exceptions.ValidationException;
import itmo.lab6.util.Validators;
import java.io.Serializable;
import java.time.LocalDate;

/** User-supplied fields only: the server assigns id and creationDate. */
public record MarineData(String name, Coordinates coordinates, double health,
                         AstartesCategory category, Weapon weaponType,
                         MeleeWeapon meleeWeapon, Chapter chapter) implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Validate again on the server: deserialization does not run model constructors. */
    public SpaceMarine create(int id, LocalDate date) throws ValidationException {
        Validators.requireNotNull(coordinates, "coordinates");
        Validators.requireNotNull(chapter, "chapter");
        if (name != null && name.length() > 4096 || chapter.getName() != null && chapter.getName().length() > 4096)
            throw new ValidationException("Names must not exceed 4096 characters");
        return new SpaceMarine(id, name, new Coordinates(coordinates.getX(), coordinates.getY()), date,
                health, category, weaponType, meleeWeapon, new Chapter(chapter.getName(), chapter.getMarinesCount()));
    }
}
