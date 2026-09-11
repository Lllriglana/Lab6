package ITMO.Lab5.model;

import ITMO.Lab5.exceptions.ValidationException;
import ITMO.Lab5.util.Validators;

import java.util.Objects;

/**
 * Coordinates of a space marine.
 */
public final class Coordinates {
    private final Double x;
    private final Float y;

    public Coordinates(Double x, Float y) throws ValidationException {
        this.x = Validators.requireGreaterThan(x, -540.0, "coordinates.x");
        this.y = Validators.requireNotNull(y, "coordinates.y");
    }

    public Double getX() {
        return x;
    }

    public Float getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
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
        Coordinates that = (Coordinates) o;
        return Objects.equals(x, that.x) && Objects.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
