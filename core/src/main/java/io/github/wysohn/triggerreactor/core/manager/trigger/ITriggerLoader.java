package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.config.IConfigSource;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;

public interface ITriggerLoader<T extends Trigger> {
    /**
     * Default filter which includes any 'File' that ends with '.trg'
     * This may need to be overridden in some cases (such as Area Trigger since Area Trigger use Folder instead of File.)
     *
     * @param file
     * @return
     */
    default boolean isTriggerFile(File file) {
        return file.isFile() && file.getName().endsWith(".trg");
    }

    default TriggerInfo[] listTriggers(File folder, BiFunction<File, String, IConfigSource> fn) {
        return Optional.ofNullable(folder.listFiles())
                .map(files -> Arrays.stream(files)
                        .filter(this::isTriggerFile)
                        .map(file -> {
                            String name = TriggerInfo.extractName(file);
                            IConfigSource config = fn.apply(folder, name + ".json");
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
