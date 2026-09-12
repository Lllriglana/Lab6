package itmo.lab6.protocol;

import itmo.lab6.model.SpaceMarine;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Result includes actual domain objects, not a string representation of a collection. */
public record Response(UUID id, boolean success, String message,
                       ArrayList<SpaceMarine> elements) implements Serializable {
    private static final long serialVersionUID = 1L;

    public static Response message(UUID id, boolean success, String message) {
        return new Response(id, success, message, new ArrayList<>());
    }

    public static Response collection(UUID id, List<SpaceMarine> values) {
        return new Response(id, true, values.isEmpty() ? "Collection is empty." : "",
                new ArrayList<>(values.stream().sorted().toList()));
    }
}
