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
import io.github.wysohn.triggerreactor.bukkit.main.BukkitGameController;
import io.github.wysohn.triggerreactor.bukkit.main.BukkitInventoryModifier;
import io.github.wysohn.triggerreactor.bukkit.main.BukkitTaskSupervisor;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.manager.IInventoryModifier;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import javax.inject.Named;

@Module
public abstract class BukkitGameControllerModule {
    @Provides
    @Named("PluginInstance")
    static Object providePlugin(Plugin plugin) {
        return plugin;
    }

    @Binds
    abstract IGameController bindGameController(BukkitGameController plugin);

    @Binds
    abstract TaskSupervisor bindTaskSupervisor(BukkitTaskSupervisor taskSupervisor);

    @Binds
    abstract IInventoryModifier bindInventoryModifier(BukkitInventoryModifier inventoryModifier);

    @Provides
    static Server provideServer() {
        return Bukkit.getServer();
    }

    @Provides
    static PluginManager bindPluginManager(Server server) {
        return server.getPluginManager();
    }
}
