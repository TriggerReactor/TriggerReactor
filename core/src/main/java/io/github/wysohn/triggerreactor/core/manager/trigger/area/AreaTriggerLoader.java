/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

@Singleton
public class AreaTriggerLoader implements ITriggerLoader<AreaTrigger> {
    @Inject
    @Named("DataFolder")
    private File dataFolder;
    @Inject
    private Logger logger;
    @Inject
    private IAreaTriggerFactory factory;

    @Inject
    private AreaTriggerLoader(){

    }

    @Override
    public TriggerInfo[] listTriggers(File folder, ConfigSourceFactory fn) {
        return Optional.ofNullable(folder.listFiles())
                .map(files -> Arrays.stream(files)
                        .filter(File::isDirectory)
                        .map(triggerFolder -> {
                            String name = triggerFolder.getName();
                            IConfigSource config = fn.create(folder, name);
                            return toTriggerInfo(triggerFolder, config);
                        })
                        .toArray(TriggerInfo[]::new))
                .orElse(new TriggerInfo[0]);
    }

    @Override
    public TriggerInfo toTriggerInfo(File triggerFolder, IConfigSource configSource) {
        // AreaTrigger itself is a folder and contains Enter.trg and Exit.trg
        return new AreaTriggerInfo(triggerFolder, configSource, triggerFolder.getName());
    }

    @Override
    public AreaTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        SimpleLocation smallest = info.get(TriggerConfigKey.KEY_TRIGGER_AREA_SMALLEST, String.class)
                .map(SimpleLocation::valueOf)
                .orElseGet(() -> new SimpleLocation("unknown", 0, 0, 0));
        SimpleLocation largest = info.get(TriggerConfigKey.KEY_TRIGGER_AREA_LARGEST, String.class)
                .map(SimpleLocation::valueOf)
                .orElseGet(() -> new SimpleLocation("unknown", 0, 0, 0));

        // this is a bit misleading because it's not really a file, but a folder
        File scriptFolder = info.getSourceCodeFile();
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
        AreaTrigger trigger = factory.create(info, area, scriptFolder);

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

        // remember that AreaTrigger is a folder, not a file
        File triggerFolder = trigger.getInfo().getSourceCodeFile();
        if (!triggerFolder.exists()) {
            triggerFolder.mkdirs();
        }

        if (trigger.getEnterTrigger() != null) {
            try {
                FileUtil.writeToFile(AbstractTriggerManager.getTriggerFile(triggerFolder, TRIGGER_NAME_ENTER, true),
                                     trigger.getEnterTrigger().getScript());
            } catch (IOException e) {
                e.printStackTrace();
                logger.warning("Could not save Area Trigger [Enter] " + trigger.getInfo());
            }
        }

        if (trigger.getExitTrigger() != null) {
            try {
                FileUtil.writeToFile(AbstractTriggerManager.getTriggerFile(triggerFolder, TRIGGER_NAME_EXIT, true),
                                     trigger.getExitTrigger().getScript());
            } catch (IOException e) {
                e.printStackTrace();
                logger.warning("Could not save Area Trigger [Exit] " + trigger.getInfo());
            }
        }
    }

    public static final String TRIGGER_NAME_EXIT = "Exit";

    public static final String TRIGGER_NAME_ENTER = "Enter";
    }
