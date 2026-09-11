package ITMO.Lab5.model;

import ITMO.Lab5.exceptions.ValidationException;
import ITMO.Lab5.util.Validators;

import java.util.Objects;

/**
 * Chapter information for a space marine.
 */
public final class Chapter {
    private final String name;
    private final int marinesCount;

    public Chapter(String name, int marinesCount) throws ValidationException {
        this.name = Validators.requireNotBlank(name, "chapter.name");
        this.marinesCount = Validators.requireRange(marinesCount, 1, 1000, "chapter.marinesCount");
    }

    public String getName() {
        return name;
    }

    public int getMarinesCount() {
        return marinesCount;
    }

    
    @Override
    public String toString() {
        return "Chapter{" +
                "name='" + name + '\'' +
                ", marinesCount=" + marinesCount +
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
        Chapter chapter = (Chapter) o;
        return marinesCount == chapter.marinesCount && Objects.equals(name, chapter.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, marinesCount);
    }
}
