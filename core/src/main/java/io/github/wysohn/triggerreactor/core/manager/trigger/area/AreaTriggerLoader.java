package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

class AreaTriggerLoader implements ITriggerLoader<AreaTrigger> {
    private final TriggerReactorCore plugin;

    public AreaTriggerLoader(TriggerReactorCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public TriggerInfo[] listTriggers(File folder, ConfigSourceFactory fn) {
        return Optional.ofNullable(folder.listFiles())
                .map(files -> Arrays.stream(files)
                        .filter(File::isDirectory)
                        .map(file -> {
                            String name = file.getName();
                            IConfigSource config = fn.create(folder, name);
                            return toTriggerInfo(file, config);
                        })
                        .toArray(TriggerInfo[]::new))
                .orElse(new TriggerInfo[0]);
    }

    @Override
    public TriggerInfo toTriggerInfo(File file, IConfigSource configSource) {
        return new AreaTriggerInfo(file, configSource, file.getName());
    }

    @Override
    public AreaTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        SimpleLocation smallest = info.get(TriggerConfigKey.KEY_TRIGGER_AREA_SMALLEST, String.class)
                .map(SimpleLocation::valueOf)
                .orElseGet(() -> new SimpleLocation("unknown", 0, 0, 0));
        SimpleLocation largest = info.get(TriggerConfigKey.KEY_TRIGGER_AREA_LARGEST, String.class)
                .map(SimpleLocation::valueOf)
                .orElseGet(() -> new SimpleLocation("unknown", 0, 0, 0));

        File scriptFolder = AbstractTriggerManager.concatPath(plugin.getDataFolder(), info.getTriggerName());
        if (!scriptFolder.exists()) {
            scriptFolder.mkdirs();
        }

        String enterScript = null;
        File enterFile = null;
        try {
            enterFile = AbstractTriggerManager.getTriggerFile(scriptFolder, "Enter", false);
            if (!enterFile.exists())
                enterFile.createNewFile();
            enterScript = FileUtil.readFromFile(enterFile);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        String exitScript = null;
        File exitFile = null;
        try {
            exitFile = AbstractTriggerManager.getTriggerFile(scriptFolder, "Exit", false);
            if (!exitFile.exists())
                exitFile.createNewFile();
            exitScript = FileUtil.readFromFile(exitFile);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        Area area = new Area(smallest, largest);
        AreaTrigger trigger = new AreaTrigger(info, area, scriptFolder);

        try {
            trigger.setEnterTrigger(enterScript);
            trigger.setExitTrigger(exitScript);
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
            return null;
        }

        return trigger;
    }

    @Override
    public void save(AreaTrigger trigger) {
        Area area = trigger.getArea();
        trigger.getInfo().put(TriggerConfigKey.KEY_TRIGGER_AREA_SMALLEST, area.getSmallest().toString());
        trigger.getInfo().put(TriggerConfigKey.KEY_TRIGGER_AREA_LARGEST, area.getLargest().toString());

        File triggerFolder = AbstractTriggerManager.concatPath(plugin.getDataFolder(),
                                                               trigger.getInfo().getTriggerName());
        if (!triggerFolder.exists()) {
            triggerFolder.mkdirs();
        }

        if (trigger.getEnterTrigger() != null) {
            try {
                FileUtil.writeToFile(AbstractTriggerManager.getTriggerFile(triggerFolder, "Enter", true),
                                     trigger.getEnterTrigger().getScript());
            } catch (IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not save Area Trigger [Enter] " + trigger.getInfo());
            }
        }

        if (trigger.getExitTrigger() != null) {
            try {
                FileUtil.writeToFile(AbstractTriggerManager.getTriggerFile(triggerFolder, "Exit", true),
                                     trigger.getExitTrigger().getScript());
            } catch (IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not save Area Trigger [Exit] " + trigger.getInfo());
            }
        }
    }
}
