package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.config.IConfigSource;
import io.github.wysohn.triggerreactor.core.config.IConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.ITypeValidator;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

public interface ITriggerLoader<T extends Trigger> {
    default TriggerInfo[] listTriggers(File folder, IConfigSourceFactory fn, ITypeValidator... validators) {
        return Optional.ofNullable(folder.listFiles())
                .map(files -> Arrays.stream(files)
                        .filter(File::isFile)
                        .filter(file -> file.getName().endsWith(".trg"))
                        .map(file -> {
                            String name = TriggerInfo.extractName(file);
                            IConfigSource config = fn.gson(folder, name + ".json", validators);
                            return toTriggerInfo(file, config);
                        })
                        .toArray(TriggerInfo[]::new))
                .orElse(new TriggerInfo[0]);
    }

    default TriggerInfo toTriggerInfo(File file, IConfigSource configSource) {
        return TriggerInfo.defaultInfo(file, configSource);
    }

    T load(TriggerInfo info) throws InvalidTrgConfigurationException;

    void save(T trigger);
}
