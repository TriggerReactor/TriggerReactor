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

package io.github.wysohn.triggerreactor.core.main;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;

import javax.inject.Inject;

public class ThrowableHandler implements IThrowableHandler {
    @Inject
    IPluginLifecycleController pluginLifecycleController;
    @Inject
    IGameController gameController;
    @Inject
    TaskSupervisor taskSupervisor;

    @Inject
    public ThrowableHandler() {

    }

    /**
     * Handle the exception caused by Executors or Triggers. The 'event' is the context when the 'event' was
     * happened. For Bukkit API, it is child classes of Event. You may extract the player instance who is
     * related to this Exception and show useful information to the game.
     *
     * @param event     the context
     * @param throwable the exception that was thrown
     */
    final public void handleException(Object event, Throwable throwable) {
        if (pluginLifecycleController.isDebugging()) {
            throwable.printStackTrace();
        }

        ICommandSender sender = gameController.extractPlayerFromContext(event);
        if (sender == null)
            sender = gameController.getConsoleSender();

        sendExceptionMessage(sender, throwable);
    }

    /**
     * Handle the exception caused by Executors or Triggers.
     *
     * @param sender    the sender who will receive the message
     * @param throwable the exception that was thrown
     */
    final public void handleException(ICommandSender sender, Throwable throwable) {
        if (pluginLifecycleController.isDebugging()) {
            throwable.printStackTrace();
        }

        if (sender == null)
            sender = gameController.getConsoleSender();

        sendExceptionMessage(sender, throwable);
    }

    @Override
    public void handleException(InterpreterLocalContext context, Throwable throwable) {
        if (pluginLifecycleController.isDebugging()) {
            throwable.printStackTrace();
        }

        Object event = context.getVar(Trigger.VAR_NAME_EVENT);
        ICommandSender sender = gameController.extractPlayerFromContext(event);
        if (sender == null)
            sender = gameController.getConsoleSender();

        sendExceptionMessage(sender, throwable);
    }

    private void sendExceptionMessage(ICommandSender sender, Throwable e) {
        taskSupervisor.runTask(() -> {
            Throwable ex = e;
            sender.sendMessage("&cCould not execute this trigger.");
            while (ex != null) {
                sender.sendMessage("&c >> Caused by:");
                sender.sendMessage("&c" + ex.getMessage());
                ex = ex.getCause();
            }
            sender.sendMessage("&cIf you are administrator, see console for details.");
        });
    }
}
