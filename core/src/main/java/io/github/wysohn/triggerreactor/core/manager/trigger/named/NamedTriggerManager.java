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
package io.github.wysohn.triggerreactor.core.manager.trigger.named;

import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

@Singleton
public final class NamedTriggerManager extends AbstractTriggerManager<NamedTrigger> {

    @Inject
    private NamedTriggerManager(@Named("DataFolder") File folder,
                                @Named("NamedTriggerManagerFolder") String folderName) {
        super(new File(folder, folderName));
    }
    @Inject
    private INamedTriggerFactory factory;

    @Inject
    private NamedTriggerLoader loader;

    @Override
    public void initialize() {

    }

    /**
     * @param name name of "Named Trigger" you want to create
     * @return true on success; false if already exist
     * @throws ParserException See {@link Trigger#init()}
     * @throws LexerException  See {@link Trigger#init()}
     * @throws IOException     See {@link Trigger#init()}
     */
    public boolean createTrigger(String name, String script)
            throws TriggerInitFailedException {
        File file = getTriggerFile(folder, name, true);
        IConfigSource config = getConfigSource(folder, name);
        TriggerInfo info = TriggerInfo.defaultInfo(file, config);
        NamedTrigger trigger = factory.create(info, script);

        trigger.init();
        put(name, trigger);
        loader.save(trigger);
        return true;
    }
}