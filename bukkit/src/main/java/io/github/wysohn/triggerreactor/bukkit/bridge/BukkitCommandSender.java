/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.bukkit.bridge;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import org.bukkit.ChatColor;

public class BukkitCommandSender implements ICommandSender {
    private final org.bukkit.command.CommandSender sender;

    public BukkitCommandSender(org.bukkit.command.CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public <T> T get() {
        return (T) sender;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sender == null) ? 0 : sender.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BukkitCommandSender other = (BukkitCommandSender) obj;
        if (sender == null) {
            if (other.sender != null)
                return false;
        } else if (!sender.equals(other.sender))
            return false;
        return true;
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(String message, boolean raw) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

}
