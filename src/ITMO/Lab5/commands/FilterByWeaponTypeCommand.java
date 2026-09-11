package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;
import ITMO.Lab5.exceptions.ValidationException;
import ITMO.Lab5.managers.CollectionManager;
import ITMO.Lab5.model.SpaceMarine;
import ITMO.Lab5.model.Weapon;
import ITMO.Lab5.util.EnumUtils;

import java.io.PrintStream;
import java.util.List;

/**
 * Prints elements with the specified weapon type.
 */
public final class FilterByWeaponTypeCommand extends AbstractCommand {
    private final CollectionManager collectionManager;
    private final PrintStream out;

    public FilterByWeaponTypeCommand(CollectionManager collectionManager, PrintStream out) {
        super("filter_by_weapon_type", "print elements with specified weaponType");
        this.collectionManager = collectionManager;
        this.out = out;
    }

    @Override
    public boolean execute(String argument) throws CommandException {
        String value = requireArgument(argument);
        Weapon weaponType;
        try {
            weaponType = EnumUtils.parseEnum(Weapon.class, value, false);
        } catch (ValidationException e) {
            throw new CommandException(e.getMessage(), e);
        }

        List<SpaceMarine> marines = collectionManager.filterByWeaponType(weaponType);
        if (marines.isEmpty()) {
            out.println("No elements found.");
            return true;
        }

        for (SpaceMarine marine : marines) {
            out.println(marine);
        }
        return true;
    }
}
