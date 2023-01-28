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

package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import com.google.inject.Guice;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.command.ICommand;
import io.github.wysohn.triggerreactor.core.main.command.ICommandHandler;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class CommandTriggerManagerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    ICommandHandler commandHandler;
    CommandTriggerLoader loader;
    CommandTriggerManager manager;

    @Before
    public void setUp() throws Exception {
        commandHandler = mock(ICommandHandler.class);
        loader = mock(CommandTriggerLoader.class);
        manager = Guice.createInjector(

        ).getInstance(CommandTriggerManager.class);
    }

    @Test
    public void reload() throws InvalidTrgConfigurationException {
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        CommandTrigger mockTrigger = mock(CommandTrigger.class);

        when(mockInfo.getTriggerName()).thenReturn("test");
        when(loader.listTriggers(any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);
        when(mockTrigger.getInfo()).thenReturn(mockInfo);

        manager.reload();

        verify(commandHandler).register(eq("test"), any());
    }

    @Test
    public void reload2() throws InvalidTrgConfigurationException {
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        CommandTrigger mockTrigger = mock(CommandTrigger.class);

        when(mockInfo.getTriggerName()).thenReturn("test");
        when(loader.listTriggers(any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);
        when(mockTrigger.getInfo()).thenReturn(mockInfo);

        ICommand command = mock(ICommand.class);
        when(commandHandler.register(eq("test"), any())).thenReturn(command);
        manager.reload();

        verify(commandHandler).register(eq("test"), any());
        verify(mockTrigger).setCommand(command);
    }

    @Test
    public void testReload() throws InvalidTrgConfigurationException {
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        CommandTrigger mockTrigger = mock(CommandTrigger.class);

        when(mockInfo.getTriggerName()).thenReturn("test");
        when(loader.listTriggers(any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);
        when(mockTrigger.getInfo()).thenReturn(mockInfo);

        manager.reload("test");
    }

    @Test
    public void remove() throws InvalidTrgConfigurationException {
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        CommandTrigger mockTrigger = mock(CommandTrigger.class);

        when(mockInfo.getTriggerName()).thenReturn("test");
        when(loader.listTriggers(any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);
        when(mockTrigger.getInfo()).thenReturn(mockInfo);

        manager.reload();
        manager.remove("test");

        verify(commandHandler).unregister("test");
        assertNull(manager.get("test"));
    }

    @Test
    public void addCommandTrigger() {
        when(commandHandler.register(eq("test"), any())).thenReturn(mock(ICommand.class));

        ICommandSender sender = mock(ICommandSender.class);
        manager.addCommandTrigger(sender, "test", "#MESSAGE \"test\"");

        verify(commandHandler).register(eq("test"), any());
        assertNotNull(manager.get("test"));
        verify(commandHandler).sync();
    }

    @Test
    public void createTempCommandTrigger() throws AbstractTriggerManager.TriggerInitFailedException {
        manager.createTempCommandTrigger("#MESSAGE \"test\"");
    }

    @Test
    public void reregisterCommand() {
        when(commandHandler.register(eq("test"), any())).thenReturn(mock(ICommand.class));

        ICommandSender sender = mock(ICommandSender.class);
        manager.addCommandTrigger(sender, "test", "#MESSAGE \"test\"");
        manager.reregisterCommand("test");

        verify(commandHandler).unregister("test");
        verify(commandHandler, times(2)).register(eq("test"), any());
        verify(commandHandler, times(2)).sync();
    }
}