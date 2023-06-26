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
package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.script.warning.Warning;
import io.github.wysohn.triggerreactor.tools.observer.IObservable;
import io.github.wysohn.triggerreactor.tools.observer.IObserver;

import javax.inject.Inject;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractTriggerManager<T extends Trigger> extends Manager {
    @Inject
    private Logger logger;
    @Inject
    private ITriggerLoader<T> loader;

    private final Observer observer = new Observer();
    private final Map<String, T> triggers = new ConcurrentHashMap<>();

    protected final File folder;
    protected final ConfigSourceFactory configSourceFactory;

    public AbstractTriggerManager(File folder) {
        this.folder = folder;
        this.configSourceFactory = ConfigSourceFactory.instance();
    }

    public File getFolder() {
        return folder;
    }

    public TriggerInfo[] getTriggerInfos() {
        return loader.listTriggers(folder, configSourceFactory);
    }

    @Override
    public void reload() {
        if (!folder.exists())
            folder.mkdirs();

        triggers.clear();

        for (TriggerInfo info : loader.listTriggers(folder, configSourceFactory)) {
            try {
                info.reloadConfig();

                T t = loader.load(info);
                t.init();

                Optional.ofNullable(t)
                        .ifPresent(trigger -> {
                            if (has(info.getTriggerName())) {
                                logger.warning(info + " is already registered! Duplicated Trigger?");
                            } else {
                                put(info.getTriggerName(), trigger);
                            }
                        });

                checkDuplicatedKeys(info);
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Failed to load " + info);
                logger.severe(e.getMessage());
            }
        }
    }

    public void reload(String triggerName) {
        IConfigSource configSource = configSourceFactory.create(folder, triggerName);
        File sourceCodeFile = new File(folder, triggerName + ".trg");
        TriggerInfo info = loader.toTriggerInfo(sourceCodeFile, configSource);

        try {
            T trigger = loader.load(info);
            put(triggerName, trigger);

            checkDuplicatedKeys(info);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + info, e);
        }
    }

    private void checkDuplicatedKeys(TriggerInfo info) {
        if (info == null)
            return;

        for (TriggerConfigKey key : TriggerConfigKey.values()) {
            if (info.hasDuplicate(key)) {
                logger.warning("Duplicated key found in " + info);
                logger.warning(String.format("Key '%s' is deprecated and is now '%s'", key.getOldKey(), key.getKey()));
            }
        }
    }

    @Override
    public void shutdown() {
        for (T trigger : triggers.values()) {
            loader.save(trigger);
        }
    }

    public T get(String name) {
        return triggers.get(name);
    }

    public boolean has(String name) {
        return triggers.containsKey(name);
    }

    public T put(String name, T t) {
        t.setObserver(observer);
        triggers.put(name, t);
        loader.save(t);
        return t;
    }

    public T remove(String name) {
        T deleted = triggers.remove(name);

        Optional.ofNullable(deleted)
                .ifPresent(loader::delete);

        return deleted;
    }

    public Collection<T> getAllTriggers() {
        return triggers.values();
    }

    public List<String> getTriggerList(TriggerFilter filter) {
        List<String> strs = new ArrayList<>();
        for (Trigger trigger : Collections.unmodifiableCollection(getAllTriggers())) {
            String str = trigger.toString();
            if (filter != null && filter.accept(str))
                strs.add(str);
            else if (filter == null)
                strs.add(str);
        }
        return strs;
    }

    @FunctionalInterface
    public interface TriggerFilter {
        boolean accept(String name);
    }

    public static File getTriggerFile(File folder, String triggerName, boolean write) {
        File triggerFile = new File(folder, triggerName + ".trg");

        //if reading the file, first check if .trg file exists and then try with no extension
        //we do not care about no extension file when we are writing.
        if (!write && !triggerFile.exists())
            triggerFile = new File(folder, triggerName);

        return triggerFile;
    }

    protected void reportWarnings(List<Warning> warnings, Trigger trigger) {
        if (warnings == null || warnings.isEmpty()) {
            return;
        }

        Level L = Level.WARNING;
        int numWarnings = warnings.size();
        String ww;
        if (numWarnings > 1) {
            ww = "warnings were";
        } else {
            ww = "warning was";
        }

        logger.log(L, "===== " + warnings.size() + " " + ww + " found while loading trigger " +
                trigger.getInfo() + " =====");
        for (Warning w : warnings) {
            for (String line : w.getMessageLines()) {
                logger.log(L, line);
            }
            logger.log(Level.WARNING, "");
        }
        logger.log(Level.WARNING, "");
    }

    private class Observer implements IObserver {
        @Override
        public void onUpdate(IObservable observable) {
            //TODO need to be done async (file I/O)
            loader.save((T) observable);
        }
    }

    public static File concatPath(File dataPath, String fileName) {
        return new File(dataPath, fileName);
    }

    @SuppressWarnings("serial")
    public static final class TriggerInitFailedException extends Exception {

        public TriggerInitFailedException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}