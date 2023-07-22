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

package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.modules.LegacyBukkitDriverModule;
import io.github.wysohn.triggerreactor.bukkit.modules.LegacyBukkitThirdPartyPluginModule;
import io.github.wysohn.triggerreactor.bukkit.tools.SerializableLocation;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class TriggerReactor extends AbstractJavaPlugin {
    public TriggerReactor() {
        super(new LegacyBukkitDriverModule(), new LegacyBukkitThirdPartyPluginModule());
    }

    @Override
    public void onEnable() {
        if (!ConfigurationSerializable.class.isAssignableFrom(Location.class)) {
            ConfigurationSerialization.registerClass(SerializableLocation.class, "org.bukkit.Location");
        }
        super.onEnable();
    }
}
