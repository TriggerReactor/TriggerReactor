/*
 * Copyright (C) 2023. TriggerReactor Team
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

package io.github.wysohn.triggerreactor.core.manager;

import com.google.inject.Injector;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class SharedVariableManager extends Manager {
    private final Map<String, AbstractAPISupport> sharedVars = new HashMap<>();
    @Inject
    private Map<String, Class<? extends AbstractAPISupport>> apiSupportDefinitions;
    /**
     * This is a workaround for the issue that Guice does not support injecting
     * the dynamically instantiated class. Do not use this other than where
     * it is really needed.
     */
    @Inject
    private Injector injector;

    @Override
    public void initialize() {
        for (Map.Entry<String, Class<? extends AbstractAPISupport>> entry : apiSupportDefinitions.entrySet()) {
            AbstractAPISupport.addSharedVar(sharedVars,
                    entry.getKey(),
                    entry.getValue(),
                    injector);
        }
    }

    @Override
    public void reload() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void saveAll() {

    }

    /**
     * Third party API support instances mapping. The map is unmodifiable and should be used
     * only for reading. The String key is the name of the third party plugin's name where they are
     * defined in their respective plugin.yml.
     *
     * @return unmodifiable map of shared variables
     */
    public Map<String, AbstractAPISupport> getSharedVars() {
        return Collections.unmodifiableMap(sharedVars);
    }
}
