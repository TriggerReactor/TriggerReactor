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
package io.github.wysohn.triggerreactor.core.manager.trigger.named;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactories;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.scope.ManagerScope;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ManagerScope
public class NamedTriggerManager extends AbstractTriggerManager<NamedTrigger> {
    @Inject
    NamedTriggerFactory factory;

    @Inject
    NamedTriggerManager(String folderName) {
        super(folderName);
    }

    @Override
    public NamedTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            return factory.create(info, script);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(NamedTrigger trigger) {
        // we don't save NamedTrigger
    }

    @Override
    public TriggerInfo[] listTriggers(File folder, ConfigSourceFactories fn) {
        File[] files = getAllFiles(new ArrayList<>(), folder);
        return Arrays.stream(files).filter(file -> file.getName().endsWith(".trg")).map(file -> {
            String name = TriggerInfo.extractName(file);
            IConfigSource config = fn.create(folder, name);
            return new NamedTriggerInfo(folder, file, config);
        }).toArray(NamedTriggerInfo[]::new);
    }

    @Override
    public void onDisable() {

    }

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
}