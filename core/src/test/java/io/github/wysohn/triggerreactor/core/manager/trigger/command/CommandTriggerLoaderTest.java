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

package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import com.google.inject.Guice;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class CommandTriggerLoaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    TriggerReactorCore core;
    CommandTriggerLoader loader;

    @Before
    public void setUp() throws Exception {
        core = mock(TriggerReactorCore.class, RETURNS_DEEP_STUBS);
        Field instanceField = TriggerReactorCore.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, core);

        loader = Guice.createInjector().getInstance(CommandTriggerLoader.class);
    }

    @Test
    public void load() throws InvalidTrgConfigurationException, IOException {
        TriggerInfo info = mock(TriggerInfo.class);
        when(info.getTriggerName()).thenReturn("test");
        when(info.getSourceCodeFile()).thenReturn(folder.newFile("test.trg"));
        when(info.get(TriggerConfigKey.KEY_TRIGGER_COMMAND_PERMISSION,
                      List.class)).thenReturn(Optional.of(Arrays.asList("test.permission")));
        when(info.get(TriggerConfigKey.KEY_TRIGGER_COMMAND_ALIASES, List.class)).thenReturn(Optional.of(Arrays.asList(
                "test.alias")));
        when(info.get(TriggerConfigKey.KEY_TRIGGER_COMMAND_TABS, List.class)).thenReturn(Optional.of(Arrays.asList(
                new HashMap<String, Object>() {{
                    put(CommandTriggerManager.TAB_INDEX, 0);
                    put(CommandTriggerManager.TAB_HINT, "<MyCoolHint>");
                    put(CommandTriggerManager.TAB_CANDIDATES, "mycoolcandidate,yourcoolcandy");
                }}
        )));

        CommandTrigger trigger = loader.load(info);

        assertEquals("test.permission", trigger.getPermissions()[0]);
        assertEquals("test.alias", trigger.getAliases()[0]);
        for(ITabCompleter tab : trigger.getTabCompleterMap().get(0)){
            assertEquals(Collections.singletonList("<MyCoolHint>"), tab.getHint());
            assertEquals(Collections.singletonList("mycoolcandidate"), tab.getCandidates("my"));

        }

    }

    @Test
    public void save() throws AbstractTriggerManager.TriggerInitFailedException, IOException {
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        when(mockInfo.getTriggerName()).thenReturn("test");
        when(mockInfo.getSourceCodeFile()).thenReturn(folder.newFile("test.trg"));

        CommandTrigger trigger = new CommandTrigger(mockInfo, "#MESSAGE \"Hello world!\"");
        trigger.setAliases(new String[]{"test.alias"});
        trigger.setPermissions(new String[]{"test.permission"});

        loader.save(trigger);

        verify(mockInfo).put(TriggerConfigKey.KEY_TRIGGER_COMMAND_PERMISSION,
                             new String[]{"test.permission"});
        verify(mockInfo).put(TriggerConfigKey.KEY_TRIGGER_COMMAND_ALIASES,
                             new String[]{"test.alias"});
        //should we save the tab completers? or only to be loaded. Need discussion
        // L> no, because tabs json should be directly applied with notepad because user should copy-paste the
        // JSON code from bLoCk service. Even if they use old-styled tab-completer command, it directly saved
    }
}