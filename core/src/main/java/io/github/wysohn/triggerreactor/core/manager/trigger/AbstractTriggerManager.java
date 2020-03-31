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

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.manager.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.script.warning.Warning;
import io.github.wysohn.triggerreactor.tools.observer.IObservable;
import io.github.wysohn.triggerreactor.tools.observer.IObserver;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractTriggerManager<T extends Trigger> extends Manager implements ConfigurationFileIO {
    private final Observer observer = new Observer();
    private Map<String, T> triggers = new ConcurrentHashMap<>();

    protected final File folder;
    protected final ITriggerLoader<T> loader;

    public AbstractTriggerManager(TriggerReactorCore plugin, File folder, ITriggerLoader<T> loader) {
        super(plugin);
        this.folder = folder;
        this.loader = loader;
    }

    @Override
    public void reload() {
        triggers.clear();

        for (TriggerInfo info : loader.listTriggers(folder, ConfigSourceFactory::gson)) {
            try {
                Optional.ofNullable(loader.instantiateTrigger(info)).ifPresent(t -> put(info.getTriggerName(), t));
            } catch (InvalidTrgConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveAll() {
        for (T trigger : triggers.values()) {
            loader.save(trigger);
        }
    }

    protected <TYPE> TYPE getData(File file, String key) throws Exception {
        return getData(file, key, null);
    }

    /**
     * @param trigger
     * @deprecated use {@link #remove(String)}
     */
    protected void deleteInfo(T trigger) {
        trigger.getInfo().delete();
    }

    public T get(String name) {
        return triggers.get(name);
    }

    public boolean has(String cmd) {
        return triggers.containsKey(cmd);
    }

    protected T put(String name, T t) {
        t.setObserver(observer);
        return triggers.put(name, t);
    }

    protected T remove(String name) {
        T deleted = triggers.remove(name);

        //TODO File I/O need to be done asynchronously
        deleted.getInfo().delete();

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

    protected static void reportWarnings(List<Warning> warnings, Trigger trigger) {
        if (warnings == null || warnings.isEmpty()) {
            return;
        }

        Level L = Level.WARNING;
        Logger log = TriggerReactorCore.getInstance().getLogger();
        int numWarnings = warnings.size();
        String ww;
        if (numWarnings > 1) {
            ww = "warnings were";
        } else {
            ww = "warning was";
        }

        log.log(L, "===== " + warnings.size() + " " + ww + " found while loading trigger " +
                trigger.getInfo() + " =====");
        for (Warning w : warnings) {
            for (String line : w.getMessageLines()) {
                log.log(L, line);
            }
            log.log(Level.WARNING, "");
        }
        log.log(Level.WARNING, "");
    }

    private class Observer implements IObserver {
        @Override
        public void onUpdate(IObservable observable) {
            loader.save((T) observable);
        }
    }

    @SuppressWarnings("serial")
    public static final class TriggerInitFailedException extends Exception {

        public TriggerInitFailedException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}