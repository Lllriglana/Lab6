package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;
import ITMO.Lab5.exceptions.ValidationException;
import ITMO.Lab5.managers.CollectionManager;
import ITMO.Lab5.model.Weapon;
import ITMO.Lab5.util.EnumUtils;

import java.io.PrintStream;

/**
 * Counts elements with weaponType less than provided value.
 */
public final class CountLessThanWeaponTypeCommand extends AbstractCommand {
    private final CollectionManager collectionManager;
    private final PrintStream out;

    public CountLessThanWeaponTypeCommand(CollectionManager collectionManager, PrintStream out) {
        super("count_less_than_weapon_type", "count elements with weaponType less than provided");
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

        long count = collectionManager.countLessThanWeaponType(weaponType);
        out.println(count);
        return true;
    }
}
