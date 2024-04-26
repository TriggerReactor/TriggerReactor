/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.config.IMigratable;
import io.github.wysohn.triggerreactor.core.config.IMigrationHelper;
import io.github.wysohn.triggerreactor.core.config.source.DelegatedConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.SaveWorker;
import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.manager.trigger.StatefulObject;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.script.interpreter.TemporaryGlobalVariableKey;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Singleton
public class GlobalVariableManager extends Manager implements IMigratable, IGlobalVariableManager, StatefulObject {
    @Inject
    private Logger logger;
    @Inject
    @Named("DataFolder")
    private File dataFolder;
    @Inject
    private IExceptionHandle exceptionHandle;
    @Inject
    private IConfigSourceFactory factory;
    @Inject
    private IPluginManagement pluginManagement;

    private IConfigSource configSource;

    @Inject
    private GlobalVariableManager() {
        super();
    }

    @Override
    public void initialize() {
        configSource = factory.create(new SaveWorker(30, (ex) -> exceptionHandle.handleException(null, ex)),
                dataFolder,
                "var");
        configSource.reload();
    }

    @Override
    public void reload() {
        logger.info("Reloading global variables...");
        configSource.reload();
        logger.info("Global variables were loaded from " + configSource);
    }

    @Override
    public void shutdown() {
        configSource.disable();
    }

    @Override
    public void saveAll() {
        configSource.saveAll();
    }

    @Override
    public boolean isMigrationNeeded() {
        File oldFile = new File(dataFolder, "var.yml");
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

        if (pluginManagement.isDebugging()) {
            logger.info("Removing global variable " + key + " by setting it to null.");
            logger.info("Current Trigger: " + Trigger.ExecutingTrigger.getExecutingTriggerSummary());
        }
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

        if (pluginManagement.isDebugging()) {
            logger.info("Setting global variable " + key + " to " + value);
            logger.info("Current Trigger: " + Trigger.ExecutingTrigger.getExecutingTriggerSummary());
        }
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
    @Override
    public HashMap<Object, Object> getGlobalVariableAdapter() {
        return adapter;
    }

    private final GlobalVariableAdapter adapter = new GlobalVariableAdapter() {

        @Override
        public Object get(Object key) {
            if (key instanceof String) {
                return GlobalVariableManager.this.get((String) key);
            } else if (key instanceof TemporaryGlobalVariableKey) {
                return super.get(key);
            } else {
                return null;
            }
        }

        @Override
        public boolean containsKey(Object key) {
            if (key instanceof String) {
                return GlobalVariableManager.this.has((String) key);
            } else if (key instanceof TemporaryGlobalVariableKey) {
                return super.containsKey(key);
            } else {
                return false;
            }
        }

        @Override
        public Object put(Object key, Object value) {
            if (key instanceof String) {
                if (value == null) {
                    remove(key);
                    return null;
                }

                try {
                    GlobalVariableManager.this.put((String) key, value);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } else if (key instanceof TemporaryGlobalVariableKey) {
                super.put(key, value);
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
                super.remove(key);
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

        private final ConcurrentHashMap<TemporaryGlobalVariableKey, Object> temp_map = new ConcurrentHashMap<>();
        protected GlobalVariableAdapter() {

        }

        @Override
        public Object get(Object key){
            return temp_map.get(key);
        }

        @Override
        public boolean containsKey(Object key) {
            return temp_map.contains(key);
        }

        @Override
        public Object put(Object key, Object value) {
            if (value == null) {
                remove(key);
                return null;
            }

            temp_map.put((TemporaryGlobalVariableKey) key, value);
            return null;
        }

        @Override
        public Object remove(Object key) {
            TemporaryGlobalVariableKey[] treeKeys = temp_map.keySet().stream()
                    .filter(k -> k.getKey().startsWith(((TemporaryGlobalVariableKey) key).getKey()))
                    .toArray(TemporaryGlobalVariableKey[]::new);

            for (TemporaryGlobalVariableKey k : treeKeys) {
                temp_map.remove(k);
            }

            return null;
        }
    }
}
