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

package io.github.wysohn.triggerreactor.core.module;

import com.google.inject.*;
import io.github.wysohn.triggerreactor.core.main.ExceptionHandle;
import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.module.manager.ManagerModule;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CorePluginModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new ManagerModule());
        install(new APISupportModule());
        install(new TRBlockModule());

        bind(IExceptionHandle.class).to(ExceptionHandle.class);
    }

    @Provides
    @Singleton
    public Set<Manager> provideManagers(Injector injector) {
        Set<Manager> managers = new HashSet<>();
        for (Key<?> key : injector.getAllBindings().keySet()) {
            if (Manager.class.isAssignableFrom(key.getTypeLiteral().getRawType())) {
                managers.add((Manager) injector.getInstance(key));
            }
        }

        return Collections.unmodifiableSet(managers);
    }

}
