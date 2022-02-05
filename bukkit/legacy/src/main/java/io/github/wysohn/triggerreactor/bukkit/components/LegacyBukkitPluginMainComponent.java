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

package io.github.wysohn.triggerreactor.bukkit.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.bukkit.main.BukkitTriggerReactor;
import io.github.wysohn.triggerreactor.bukkit.scope.BukkitPluginBootstrapScope;
import io.github.wysohn.triggerreactor.core.main.CommandHandler;
import io.github.wysohn.triggerreactor.core.main.IWrapper;

@Component(dependencies = {BukkitTriggerReactorComponent.class,})
@BukkitPluginBootstrapScope
public interface LegacyBukkitPluginMainComponent {
    BukkitTriggerReactor bukkitTriggerReactor();

    CommandHandler commandHandler();

    IWrapper wrapper();
}
