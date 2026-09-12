package itmo.lab6.protocol;

/** Commands accepted over the network. Save and exit are deliberately absent. */
public enum CommandType {
    HELP, INFO, SHOW, ADD, UPDATE, REMOVE_BY_ID, CLEAR, ADD_IF_MIN, SHUFFLE,
    COUNT_LESS_THAN_WEAPON_TYPE, FILTER_BY_WEAPON_TYPE, PRINT_UNIQUE_CATEGORY
}
