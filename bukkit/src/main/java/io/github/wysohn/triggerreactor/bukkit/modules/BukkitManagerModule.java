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
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.BukkitEventRegistryManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.IEventRegistry;
import io.github.wysohn.triggerreactor.core.modules.CoreManagerModule;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Named;
import java.io.File;

@Module(includes = {CoreManagerModule.class})
public abstract class BukkitManagerModule {
    @Provides
    @Named("DataFolder")
    static File bindDataFolder(JavaPlugin plugin) {
        return plugin.getDataFolder();
    }

    @Provides
    @Named("ItemStack")
    static Class<?> bindItemStackClass(){
        return ItemStack.class;
    }

    @Binds
    abstract IEventRegistry bindEventRegistry(BukkitEventRegistryManager registry);


}
