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
package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.components.DaggerBukkitPluginMainComponent;
import io.github.wysohn.triggerreactor.bukkit.components.DaggerLatestBukkitPluginMainComponent;
import io.github.wysohn.triggerreactor.bukkit.components.LatestBukkitPluginMainComponent;
import io.github.wysohn.triggerreactor.core.main.DaggerPluginMainComponent;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorMain;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class TriggerReactor extends AbstractJavaPlugin {
    private LatestBukkitPluginMainComponent component;

    @Override
    protected TriggerReactorMain getMain() {
        component = DaggerLatestBukkitPluginMainComponent.builder()
                .bukkitPluginMainComponent(DaggerBukkitPluginMainComponent.builder()
                        .pluginMainComponent(DaggerPluginMainComponent.create())
                        .build())
                .build();
        component.inject(this);
        return component.main();
    }

    @Override
    protected Set<Manager> getManagers() {
        return component.managers();
    }

    @Override
    public Map<String, Command> getCommandMap(TriggerReactorMain plugin) {
        try {
            Server server = Bukkit.getServer();

            Field f = server.getClass().getDeclaredField("commandMap");
            f.setAccessible(true);

            CommandMap scm = (CommandMap) f.get(server);

            Method knownCommands = scm.getClass().getDeclaredMethod("getKnownCommands");
            return (Map<String, Command>) knownCommands.invoke(scm);
        } catch (Exception ex) {
            if (component.pluginLifecycle().isDebugging())
                ex.printStackTrace();

            getLogger().warning("Couldn't find 'commandMap'. This may indicate that you are using very very old" +
                    " version of Bukkit. Please report this to TR team, so we can work on it.");
            getLogger().warning("Use /trg debug to see more details.");
            return null;
        }
    }

    private Method syncMethod = null;
    private boolean notFound = false;

    @Override
    public void synchronizeCommandMap() {
        if (notFound) // in case of the syncCommands method doesn't exist, just skip it
            return; // command still works without synchronization anyway

        Server server = Bukkit.getServer();
        if (syncMethod == null) {
            try {
                syncMethod = server.getClass().getDeclaredMethod("syncCommands");
                syncMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                if (isDebugging())
                    e.printStackTrace();

                getLogger().warning("Couldn't find syncCommands(). This is not an error! Though, tab-completer" +
                        " may not work with this error. Report to us if you believe this version has to support it.");
                getLogger().warning("Use /trg debug to see more details.");
                notFound = true;
                return;
            }
        }

        try {
            syncMethod.invoke(server);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
