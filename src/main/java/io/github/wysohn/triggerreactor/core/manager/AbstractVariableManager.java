package io.github.wysohn.triggerreactor.core.manager;

import java.util.regex.Pattern;

import io.github.wysohn.triggerreactor.bukkit.manager.VariableManager.GlobalVariableAdapter;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;

public abstract class AbstractVariableManager extends Manager {

    private static final Pattern pattern = Pattern.compile(
                "# Match a valid Windows filename (unspecified file system).          \n" +
                "^                                # Anchor to start of string.        \n" +
                "(?!                              # Assert filename is not: CON, PRN, \n" +
                "  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n" +
                "    CON|PRN|AUX|NUL|             # COM5, COM6, COM7, COM8, COM9,     \n" +
                "    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n" +
                "  )                              # LPT6, LPT7, LPT8, and LPT9...     \n" +
                "  (?:\\.[^.]*)?                  # followed by optional extension    \n" +
                "  $                              # and end of string                 \n" +
                ")                                # End negative lookahead assertion. \n" +
                "[^<>:\"/\\\\|?*\\x00-\\x1F]*     # Zero or more valid filename chars.\n" +
                "[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n" +
                "$                                # Anchor to end of string.            ",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS);

    public abstract void remove(String key);

    public abstract boolean has(String key);

    public abstract void put(String key, Object value);

    public abstract Object get(String key);

    public abstract GlobalVariableAdapter getGlobalVariableAdapter();

    public static boolean isValidName(String str) {
        return pattern.matcher(str).matches();
    }

    public AbstractVariableManager(TriggerReactor plugin) {
        super(plugin);
    }

}