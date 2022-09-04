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

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitWrapper;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.coreprotect.CoreprotectSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.mcmmo.McMmoSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.placeholder.PlaceHolderSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.protocollib.ProtocolLibSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.vault.VaultSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.worldguard.WorldguardSupport;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class TriggerReactor extends AbstractJavaPlugin {
    private SelfReference selfReference;

    @Override
    public void onEnable() {
        selfReference = new CommonFunctions(core);
        BukkitTriggerReactorCore.WRAPPER = new BukkitWrapper();
        super.onEnable();
    }

    @Override
    protected void registerAPIs() {
        APISupport.addSharedVars("coreprotect", CoreprotectSupport.class);
        APISupport.addSharedVars("mcmmo", McMmoSupport.class);
        APISupport.addSharedVars("placeholder", PlaceHolderSupport.class);
        APISupport.addSharedVars("protocollib", ProtocolLibSupport.class);
        APISupport.addSharedVars("vault", VaultSupport.class);
        APISupport.addSharedVars("worldguard", WorldguardSupport.class);
    }

    @Override
    public SelfReference getSelfReference() {
        return selfReference;
    }

    @Override
    public Map<String, Command> getCommandMap() {
        try {
            Server server = Bukkit.getServer();

            Field f = server.getClass().getDeclaredField("commandMap");
            f.setAccessible(true);

            CommandMap scm = (CommandMap) f.get(server);

            Method knownCommands = scm.getClass().getDeclaredMethod("getKnownCommands");
            return (Map<String, Command>) knownCommands.invoke(scm);
        } catch (Exception ex) {
            if (core.isDebugging())
                ex.printStackTrace();

            core.getLogger().warning("Couldn't find 'commandMap'. This may indicate that you are using very very old" +
                    " version of Bukkit. Please report this to TR team, so we can work on it.");
            core.getLogger().warning("Use /trg debug to see more details.");
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
