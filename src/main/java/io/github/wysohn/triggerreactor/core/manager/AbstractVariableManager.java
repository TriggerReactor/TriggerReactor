package io.github.wysohn.triggerreactor.core.manager;

import java.util.HashMap;
import java.util.regex.Pattern;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;

public abstract class AbstractVariableManager extends Manager{

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

    public AbstractVariableManager(TriggerReactor plugin) {
        super(plugin);
    }

    /**
     * Remove global variable named 'key.' The 'key' might can contains '.' to indicate the grouping
     * of yaml.
     * @param key the key
     */
    public abstract void remove(String key);

    /**
     * Check if the key is set
     * @param key the key
     * @return true if set; false if nothing is set with 'key'
     */
    public abstract boolean has(String key);

    /**
     * Save new value. This should replace the value if already exists.
     * @param key the key. (This can contains '.' to indicate grouping of yaml)
     * @param value the value to save
     * @throws Exception
     */
    public abstract void put(String key, Object value) throws Exception;

    /**
     * get value saved with the 'key'
     * @param key the key
     * @return the value object if exists; null if nothing found
     */
    public abstract Object get(String key);

    /**
     * Get global variable adapter that will be used by Triggers. The adapter should extends HashMap and
     * override get() put() has() remove() methods in order to work properly.
     * @return
     */
    public abstract HashMap<String, Object> getGlobalVariableAdapter();

    /**
     * Check if the string is valid as key.
     * @param str the string to test
     * @return true if valid; false if cannot be used as key
     */
    public static boolean isValidName(String str) {
        return pattern.matcher(str).matches();
    }

    @SuppressWarnings("serial")
    public static abstract class GlobalVariableAdapter extends HashMap<String, Object>{
        protected GlobalVariableAdapter(){

        }
        @Override
        public abstract Object get(Object key);

        @Override
        public abstract boolean containsKey(Object key);

        @Override
        public abstract Object put(String key, Object value);
    }
}