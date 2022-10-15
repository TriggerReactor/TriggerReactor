package io.github.wysohn.triggerreactor.core.manager.trigger.named;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class NamedTriggerLoader implements ITriggerLoader<NamedTrigger> {
    private File[] getAllFiles(List<File> list, File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File each : files) {
                    getAllFiles(list, each);
                }
            }
        } else {
            list.add(file);
        }

        return list.toArray(new File[0]);
    }

    @Override
    public TriggerInfo[] listTriggers(File folder, ConfigSourceFactory fn) {
        File[] files = getAllFiles(new ArrayList<>(), folder);
        return Arrays.stream(files)
                .filter(file -> file.getName().endsWith(".trg"))
                .map(file -> {
                    String name = TriggerInfo.extractName(file);
                    IConfigSource config = fn.create(folder, name);
                    return new NamedTriggerInfo(folder, file, config);
                }).toArray(NamedTriggerInfo[]::new);
    }

    @Override
    public NamedTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            return new NamedTrigger(info, script);
        } catch (AbstractTriggerManager.TriggerInitFailedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(NamedTrigger trigger) {
        // we don't save NamedTrigger
    }
}
