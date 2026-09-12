package itmo.lab6.protocol;

import itmo.lab6.model.Weapon;
import java.io.Serializable;
import java.util.UUID;

/** Typed command with typed optional arguments; UUID identifies retransmissions. */
public record Request(UUID id, CommandType command, Integer elementId,
                      Weapon weapon, MarineData element) implements Serializable {
    private static final long serialVersionUID = 1L;
}
