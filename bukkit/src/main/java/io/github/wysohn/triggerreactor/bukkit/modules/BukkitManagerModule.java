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
import dagger.multibindings.IntoSet;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.BukkitEventRegistryManager;
import io.github.wysohn.triggerreactor.bukkit.scope.BukkitManager;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.selection.AreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.IEventRegistry;

@Module
public abstract class BukkitManagerModule {
    @Binds
    @BukkitManager
    abstract AreaSelectionManager bindAreaSelectionManager(AreaSelectionManager manager);

    @Binds
    @BukkitManager
    abstract ExecutorManager bindExecutorManager(ExecutorManager manager);

    @Binds
    @BukkitManager
    abstract InventoryEditManager bindInvEditManager(InventoryEditManager manager);

    @Binds
    @BukkitManager
    abstract AbstractPermissionManager bindPermissionManager(PermissionManager manager);

    @Binds
    @BukkitManager
    abstract PlaceholderManager bindPlaceholderManager(PlaceholderManager manager);

    @Binds
    @BukkitManager
    abstract PlayerLocationManager bindPlayerLocManager(PlayerLocationManager manager);

    @Binds
    @BukkitManager
    abstract ScriptEditManager bindScriptEditManager(ScriptEditManager manager);

    @Binds
    @IntoSet
    @BukkitManager
    abstract Manager bindIntoSetAreaSelectionManager(AreaSelectionManager manager);

    @Binds
    @IntoSet
    @BukkitManager
    abstract Manager bindIntoSetExecutorManager(ExecutorManager manager);

    @Binds
    @IntoSet
    @BukkitManager
    abstract Manager bindIntoSetInventoryEditManager(InventoryEditManager manager);

    @Binds
    @IntoSet
    @BukkitManager
    abstract Manager bindIntoSetPermissionManager(AbstractPermissionManager manager);

    @Binds
    @IntoSet
    @BukkitManager
    abstract Manager bindIntoSetPlaceholderManager(PlaceholderManager manager);

    @Binds
    @IntoSet
    @BukkitManager
    abstract Manager bindIntoSetPlayerLocationManager(PlayerLocationManager manager);

    @Binds
    @IntoSet
    @BukkitManager
    abstract Manager bindIntoSetScriptEditManager(ScriptEditManager manager);

    @Binds
    @BukkitManager
    abstract IEventRegistry bindEventRegistry(BukkitEventRegistryManager registry);
}
