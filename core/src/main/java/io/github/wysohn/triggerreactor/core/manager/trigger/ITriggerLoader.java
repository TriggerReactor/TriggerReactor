package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.SaveWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface ITriggerLoader<T extends Trigger> {
    default void search(SaveWorker saveWorker, File folder, IConfigSourceFactory fn, List<TriggerInfo> list) {
        Optional.ofNullable(folder.listFiles())
                .ifPresent(files -> Arrays.stream(files)
                        .forEach(file -> {
                            if (file.isFile() && file.getName().endsWith(".trg")) {
                                String name = TriggerInfo.extractName(file);
                                IConfigSource config = fn.create(saveWorker, folder, name);
                                list.add(toTriggerInfo(file, config));
                            } else if (file.isDirectory()) {
                                search(saveWorker, file, fn, list);
                            }
                        }));
    }

    default TriggerInfo[] listTriggers(SaveWorker saveWorker, File folder, IConfigSourceFactory fn) {
        List<TriggerInfo> list = new ArrayList<>();
        search(saveWorker, folder, fn, list);
        return list.toArray(new TriggerInfo[0]);
    }

    default TriggerInfo toTriggerInfo(File sourceCodeFile, IConfigSource configSource) {
        return TriggerInfo.defaultInfo(sourceCodeFile, configSource);
    }

    T load(TriggerInfo info) throws InvalidTrgConfigurationException;

    //TODO File I/O need to be done asynchronously
    void save(T trigger);

    default void delete(T trigger) {
        //TODO File I/O need to be done asynchronously
        trigger.getInfo().delete();
    }
}
