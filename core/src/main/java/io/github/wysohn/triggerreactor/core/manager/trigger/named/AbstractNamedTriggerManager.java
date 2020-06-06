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

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.config.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.manager.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public abstract class AbstractNamedTriggerManager extends AbstractTriggerManager<NamedTrigger> {

    public AbstractNamedTriggerManager(TriggerReactorCore plugin, File folder) {
        super(plugin, folder, new ITriggerLoader<NamedTrigger>() {
            private File[] getAllFiles(List<File> list, File file){
                if(file.isDirectory()){
                    File[] files = file.listFiles();
                    if(files != null){
                        for(File each : files){
                            getAllFiles(list, each);
                        }
                    }
                } else {
                    list.add(file);
                }

                return list.toArray(new File[0]);
            }

            @Override
            public TriggerInfo[] listTriggers(File folder, BiFunction<File, String, IConfigSource> fn) {
                File[] files = getAllFiles(new ArrayList<>(), folder);
                return Arrays.stream(files)
                        .filter(file -> file.getName().endsWith(".trg"))
                        .map(file -> {
                            String name = TriggerInfo.extractName(file);
                            IConfigSource config = fn.apply(folder, name + ".json");
                            return new NamedTriggerInfo(folder, file, config);
                        }).toArray(NamedTriggerInfo[]::new);
            }

            @Override
            public NamedTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
                try {
                    String script = FileUtil.readFromFile(info.getSourceCodeFile());
                    return new NamedTrigger(info, script);
                } catch (TriggerInitFailedException | IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void save(NamedTrigger trigger) {
                // we don't save NamedTrigger
            }
        }, ConfigSourceFactory::none);
    }
}