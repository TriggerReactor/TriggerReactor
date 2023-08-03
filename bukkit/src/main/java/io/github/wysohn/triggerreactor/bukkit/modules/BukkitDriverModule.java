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

package io.github.wysohn.triggerreactor.bukkit.modules;

import com.google.inject.AbstractModule;
import io.github.wysohn.triggerreactor.bukkit.main.*;
import io.github.wysohn.triggerreactor.core.main.*;
import io.github.wysohn.triggerreactor.core.main.command.ICommandHandler;
import io.github.wysohn.triggerreactor.core.module.IOUtilityModule;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;

public class BukkitDriverModule extends AbstractModule {
    @Override
    public void configure() {
        install(new IOUtilityModule());

        bind(ICommandHandler.class).to(BukkitCommandHandler.class);
        bind(IEventManagement.class).to(BukkitEventManagement.class);
        bind(IEventRegistry.class).to(BukkitEventRegistry.class);
        bind(IGameManagement.class).to(BukkitGameManagement.class);
        bind(IInventoryHandle.class).to(BukkitInventoryHandle.class);
        bind(IPluginManagement.class).to(BukkitPluginManagement.class);
        bind(TaskSupervisor.class).to(BukkitTaskSupervisor.class);
    }
}
