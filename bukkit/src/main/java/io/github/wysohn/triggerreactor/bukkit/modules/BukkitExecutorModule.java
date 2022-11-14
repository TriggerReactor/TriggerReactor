/*
 * Copyright (C) 2022. TriggerReactor Team
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

package io.github.wysohn.triggerreactor.bukkit.modules;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.ProvidesIntoMap;
import com.google.inject.multibindings.StringMapKey;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.Callable;

public class BukkitExecutorModule extends AbstractModule {
    @ProvidesIntoMap
    @StringMapKey("CMDOP")
    public Executor provideExecutorOverride(TaskSupervisor taskSupervisor) {
        return (timing, variables, e, args) -> {
            Object player = variables.get("player");
            if (!(player instanceof Player))
                return null;

            DispatchCommandAsOP call = new DispatchCommandAsOP((Player) player, String.valueOf(args[0]));
            if (taskSupervisor.isServerThread()) {
                call.call();
            } else {
                try {
                    taskSupervisor.submitSync(call).get();
                } catch (Exception ex) {
                    //to double check
                    call.deOpIfWasNotOp();
                }
            }

            return null;
        };
    }

    private class DispatchCommandAsOP implements Callable<Void> {
        private final Player player;
        private final String cmd;

        private boolean wasOp;

        DispatchCommandAsOP(Player player, String cmd) {
            super();
            this.player = player;
            this.cmd = cmd;
        }

        void deOpIfWasNotOp() {
            if (!wasOp)
                player.setOp(false);
        }

        @Override
        public Void call() throws Exception {
            wasOp = player.isOp();

            try {
                player.setOp(true);

                Bukkit.dispatchCommand(player, cmd); // now we register CommandTrigger to commandMap
            } catch (Exception e) {

            } finally {
                deOpIfWasNotOp();
            }
            return null;
        }

    }
}
