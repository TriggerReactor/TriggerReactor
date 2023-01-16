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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.wysohn.triggerreactor.bukkit.manager.CommandHandleManager;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.ICommandMapHandler;
import org.bukkit.command.Command;

import java.util.Map;

@Singleton
public class LegacyBukkitCommandMapHandler implements ICommandMapHandler {
    @Inject
    private CommandHandleManager handler;

    @Inject
    public LegacyBukkitCommandMapHandler() {
    }

    @Override
    public Map<String, Command> getCommandMap() {
        return handler.getMapAdapter();
//        try {
//            Server server = Bukkit.getServer();
//
//            Field f = server.getClass().getDeclaredField("commandMap");
//            f.setAccessible(true);
//
//            CommandMap scm = (CommandMap) f.get(server);
//
//            Field f2 = scm.getClass().getDeclaredField("knownCommands");
//            f2.setAccessible(true);
//
//            return (Map<String, Command>) f2.get(scm);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//
//            core.getLogger().warning("Couldn't bind 'commandMap'. This may indicate that you are using very very
//            old" +
//                    " version of Bukkit. Please report this to TR team, so we can work on it.");
//            core.getLogger().warning("Use /trg debug to see more details.");
//            return null;
//        }
    }

    @Override
    public void synchronizeCommandMap() {
        // do nothing. Not really necessary atm for legacy versions
    }


}
