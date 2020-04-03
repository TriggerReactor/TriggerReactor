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
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractNamedTriggerManager extends AbstractTriggerManager<NamedTrigger> {

    public AbstractNamedTriggerManager(TriggerReactorCore plugin, File folder) {
        super(plugin, folder, new ITriggerLoader<NamedTrigger>() {
            @Override
            public TriggerInfo[] listTriggers(File folder, BiFunction<File, String, IConfigSource> fn) {
                return Optional.ofNullable(folder.listFiles())
                        .map(Arrays::stream)
                        .map(stream -> {
                            Stream<File> folderToFilesStream = stream.filter(File::isDirectory)
                                    .map(file -> listTriggers(file, fn))
                                    .flatMap(Arrays::stream)
                                    .map(TriggerInfo::getSourceCodeFile);

                            return Stream.concat(stream.filter(File::isFile), folderToFilesStream);
                        })
                        .map(stream -> stream.map(file -> {
                            String name = TriggerInfo.extractName(file);
                            IConfigSource config = fn.apply(folder, name);
                            return new NamedTriggerInfo(folder, file, config);
                        }).collect(Collectors.toList()).toArray(new NamedTriggerInfo[0]))
                        .orElse(new NamedTriggerInfo[0]);
            }

            @Override
            public NamedTrigger instantiateTrigger(TriggerInfo info) throws InvalidTrgConfigurationException {
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