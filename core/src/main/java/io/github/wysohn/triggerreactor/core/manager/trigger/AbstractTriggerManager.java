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
package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactories;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorMain;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.warning.Warning;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.observer.IObservable;
import io.github.wysohn.triggerreactor.tools.observer.IObserver;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractTriggerManager<T extends Trigger> extends Manager implements ITriggerLoader<T> {
    @Inject
    protected TriggerReactorMain main;
    @Inject
    protected IThrowableHandler throwableHandler;
    @Inject
    protected IGameController gameController;
    @Inject
    protected SelfReference selfReference;
    @Inject
    protected TaskSupervisor taskSupervisor;
    @Inject
    protected ConfigSourceFactories configSourceFactories;
    @Inject
    protected Logger logger;
    @Inject
    @Named("DataFolder")
    protected File dataFolder;

    private final String folderName;
    private final Observer observer = new Observer();
    private final Map<String, T> triggers = new ConcurrentHashMap<>();

    protected File folder;

    public AbstractTriggerManager(String folderName) {
        this.folderName = folderName;
    }

    public File getFolder() {
        return folder;
    }

    public TriggerInfo[] getTriggerInfos() {
        return listTriggers(folder, configSourceFactories);
    }

    @Override
    public void onEnable() throws Exception {
        folder = new File(dataFolder, folderName);
    }

    @Override
    public void onReload() {
        if (!folder.exists())
            folder.mkdirs();

        triggers.clear();

        for (TriggerInfo info : listTriggers(folder, configSourceFactories)) {
            try {
                info.reloadConfig();

                T t = load(info);
                Optional.ofNullable(t)
                        .ifPresent(trigger -> {
                            if (has(info.getTriggerName())) {
                                logger.warning(info + " is already registered! Duplicated Trigger?");
                            } else {
                                put(info.getTriggerName(), trigger);
                            }
                        });
            } catch (Exception e) {
                throw new RuntimeException("Failed to load " + info, e);
            }
        }
    }

    public void reload(String triggerName) {
        T trigger = get(triggerName);
        TriggerInfo info = trigger.info;

        try {
            trigger = load(info);
            put(triggerName, trigger);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + info, e);
        }
    }

    @Override
    public void saveAll() {
        for (T trigger : triggers.values()) {
            save(trigger);
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
        return triggers.put(name, t);
    }

    public T remove(String name) {
        T deleted = triggers.remove(name);

        //TODO File I/O need to be done asynchronously
        Optional.ofNullable(deleted)
                .map(T::getInfo)
                .ifPresent(TriggerInfo::delete);

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

    void reportWarnings(List<Warning> warnings, Trigger trigger) {
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
            save((T) observable);
        }
    }

    @SuppressWarnings("serial")
    public static final class TriggerInitFailedException extends Exception {

        public TriggerInitFailedException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}