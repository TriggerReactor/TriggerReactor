package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface ITriggerLoader<T extends Trigger> {
    default void search(File folder, ConfigSourceFactory fn, List<TriggerInfo> list) {
        Optional.ofNullable(folder.listFiles())
                .ifPresent(files -> Arrays.stream(files)
                        .filter(file -> file.getName().endsWith(".trg"))
                        .forEach(file -> {
                            if (file.isFile()) {
                                String name = TriggerInfo.extractName(file);
                                IConfigSource config = fn.create(folder, name);
                                list.add(toTriggerInfo(file, config));
                            } else {
                                search(file, fn, list);
                            }
                        }));
    }

    default TriggerInfo[] listTriggers(File folder, ConfigSourceFactory fn) {
        List<TriggerInfo> list = new ArrayList<>();
        search(folder, fn, list);
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
