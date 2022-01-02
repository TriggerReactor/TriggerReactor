package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactories;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public interface ITriggerLoader<T extends Trigger> {
    T load(TriggerInfo info) throws InvalidTrgConfigurationException;

    default void save(T trigger){
        try {
            FileUtil.writeToFile(trigger.getSourceCodeFile(), trigger.getScript());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    default TriggerInfo[] listTriggers(File folder, ConfigSourceFactories fn) {
        return Optional.ofNullable(folder.listFiles())
                .map(files -> Arrays.stream(files)
                        .filter(File::isFile)
                        .filter(file -> file.getName().endsWith(".trg"))
                        .map(file -> {
                            String name = TriggerInfo.extractName(file);
                            IConfigSource config = fn.create(folder, name);
                            return toTriggerInfo(file, config);
                        })
                        .toArray(TriggerInfo[]::new))
                .orElse(new TriggerInfo[0]);
    }

    default TriggerInfo toTriggerInfo(File file, IConfigSource configSource) {
        return TriggerInfo.defaultInfo(file, configSource);
    }
}
