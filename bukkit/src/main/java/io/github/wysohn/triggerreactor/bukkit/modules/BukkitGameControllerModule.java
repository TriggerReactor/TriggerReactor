/*
 *     Copyright (C) 2021 wysohn and contributors
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.bukkit.modules;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import io.github.wysohn.triggerreactor.bukkit.main.AbstractJavaPlugin;
import io.github.wysohn.triggerreactor.bukkit.main.BukkitTaskSupervisor;
import io.github.wysohn.triggerreactor.bukkit.scope.BukkitPluginScope;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import javax.inject.Named;

@Module
public abstract class BukkitGameControllerModule {
    @Binds
    @BukkitPluginScope
    @Named("PluginInstance")
    abstract Object providePluginInstance(AbstractJavaPlugin plugin);

    @Binds
    @BukkitPluginScope
    abstract IGameController bindGameController(AbstractJavaPlugin plugin);

    @Binds
    @BukkitPluginScope
    abstract TaskSupervisor bindTaskSupervisor(BukkitTaskSupervisor taskSupervisor);

    @Provides
    @BukkitPluginScope
    static Server provideServer() {
        return Bukkit.getServer();
    }
}
