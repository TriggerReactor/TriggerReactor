package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.manager.config.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.config.InvalidTrgConfigurationException;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;

public interface ITriggerLoader<T extends Trigger> {
    default TriggerInfo[] listTriggers(File folder, BiFunction<File, String, IConfigSource> fn) {
        return Optional.ofNullable(folder.listFiles())
                .map(files -> Arrays.stream(files)
                        .filter(File::isFile)
                        .map(file -> {
                            String name = TriggerInfo.extractName(file);
                            IConfigSource config = fn.apply(folder, name);
                            return toTriggerInfo(file, config);
                        })
                        .toArray(TriggerInfo[]::new))
                .orElse(new TriggerInfo[0]);
    }

    default TriggerInfo toTriggerInfo(File file, IConfigSource configSource) {
        return TriggerInfo.defaultInfo(file, configSource);
    }

    T instantiateTrigger(TriggerInfo info) throws InvalidTrgConfigurationException;

    void save(T trigger);
}
