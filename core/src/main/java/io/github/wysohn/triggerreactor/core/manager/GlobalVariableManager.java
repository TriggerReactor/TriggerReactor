/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.config.IMigratable;
import io.github.wysohn.triggerreactor.core.config.IMigrationHelper;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.DelegatedConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.script.interpreter.TemporaryGlobalVariableKey;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class GlobalVariableManager extends Manager implements IMigratable {
    private final IConfigSource configSource;

    public GlobalVariableManager(TriggerReactorCore plugin) {
        this(plugin, ConfigSourceFactory.instance().create(plugin.getDataFolder(), "var"));
    }

    public GlobalVariableManager(TriggerReactorCore plugin, IConfigSource configSource) {
        super(plugin);
        this.configSource = configSource;
    }

    @Override
    public void reload() {
        plugin.getLogger().info("Reloading global variables...");
        configSource.reload();
        plugin.getLogger().info("Global variables were loaded from " + configSource);
    }

    @Override
    public void saveAll() {
        configSource.saveAll();
    }

    @Override
    public void disable() {
        configSource.disable();
    }

    @Override
    public boolean isMigrationNeeded() {
        File oldFile = new File(plugin.getDataFolder(), "var.yml");
        // after migration, file will be renamed to .yml.bak, and .json file will be created.
        // so migrate only if old file exist and new file is not yet generated.
        return oldFile.exists() && !configSource.fileExists();
    }

    @Override
    public void migrate(IMigrationHelper migrationHelper) {
        DelegatedConfigSource delegatedConfigSource = new DelegatedConfigSource(configSource) {
            @Override
            public void put(String key, Object value) {
                GlobalVariableManager.this.put(key, value);
            }
        };
        migrationHelper.migrate(delegatedConfigSource);
    }

    /**
     * Remove global variable named 'key.' The 'key' might can contains '.' to indicate the grouping
     * of yaml.
     *
     * @param key the key
     */
    public void remove(String key) {
        configSource.put(key, null);
    }

    /**
     * Check if the key is set
     *
     * @param key the key
     * @return true if set; false if nothing is set with 'key'
     */
    public boolean has(String key) {
        return configSource.has(key);
    }

    /**
     * Save new value. This should replace the value if already exists.
     *
     * @param key   the key. (This can contains '.' to indicate grouping of yaml)
     * @param value the value to save
     * @throws Exception
     */
    public void put(String key, Object value) {
        configSource.put(key, value);
    }

    /**
     * get value saved with the 'key'
     *
     * @param key the key
     * @return the value object if exists; null if nothing found
     */
    public Object get(String key) {
        return configSource.get(key).orElse(null);
    }

    /**
     * Get global variable adapter that will be used by Triggers. The adapter should extends HashMap and
     * override get() put() has() remove() methods in order to work properly.
     *
     * @return
     */
    public HashMap<Object, Object> getGlobalVariableAdapter() {
        return adapter;
    }

    private final GlobalVariableAdapter adapter = new GlobalVariableAdapter() {
        private final ConcurrentHashMap<TemporaryGlobalVariableKey, Object> temp_map = new ConcurrentHashMap<>();

        @Override
        public Object get(Object key) {
            if (key instanceof String) {
                return GlobalVariableManager.this.get((String) key);
            } else if (key instanceof TemporaryGlobalVariableKey) {
                return temp_map.get(key);
            } else {
                return null;
            }
        }

        @Override
        public boolean containsKey(Object key) {
            if (key instanceof String) {
                return GlobalVariableManager.this.has((String) key);
            } else if (key instanceof TemporaryGlobalVariableKey) {
                return temp_map.contains(key);
            } else {
                return false;
            }
        }

        @Override
        public Object put(Object key, Object value) {
            if (key instanceof String) {
                try {
                    GlobalVariableManager.this.put((String) key, value);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } else if (key instanceof TemporaryGlobalVariableKey) {
                temp_map.put((TemporaryGlobalVariableKey) key, value);
            }

            return null;
        }

        @Override
        public Object remove(Object key) {
            if (key instanceof String) {
                try {
                    GlobalVariableManager.this.remove((String) key);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } else if (key instanceof TemporaryGlobalVariableKey) {
                temp_map.remove(key);
            }

            return null;
        }
    };

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

    /**
     * Check if the string is valid as key.
     *
     * @param str the string to test
     * @return true if valid; false if cannot be used as key
     */
    public static boolean isValidName(String str) {
        return pattern.matcher(str).matches();
    }

    @SuppressWarnings("serial")
    public static abstract class GlobalVariableAdapter extends HashMap<Object, Object> {
        protected GlobalVariableAdapter() {

        }

        @Override
        public abstract Object get(Object key);

        @Override
        public abstract boolean containsKey(Object key);

        @Override
        public abstract Object put(Object key, Object value);
    }
}