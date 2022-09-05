package io.github.wysohn.triggerreactor.core.manager.trigger.command;

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

        when(core.getExecutorManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getPlaceholderManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getVariableManager().getGlobalVariableAdapter()).thenReturn(new HashMap<>());

        loader = new CommandTriggerLoader();
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
                    put(CommandTriggerManager.HINT, "<hint>");
                    put(CommandTriggerManager.CANDIDATES, "test,candidates");
                }}
        )));

        CommandTrigger trigger = loader.load(info);

        assertEquals("test.permission", trigger.getPermissions()[0]);
        assertEquals("test.alias", trigger.getAliases()[0]);
        assertEquals(Collections.singletonList("<hint>"), trigger.getTabCompleters()[0].getHint());
        assertEquals(Collections.singletonList("test"), trigger.getTabCompleters()[0].getCandidates("te"));
    }

    @Test
    public void save() throws AbstractTriggerManager.TriggerInitFailedException, IOException {
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        when(mockInfo.getTriggerName()).thenReturn("test");
        when(mockInfo.getSourceCodeFile()).thenReturn(folder.newFile("test.trg"));

        CommandTrigger trigger = new CommandTrigger(mockInfo, "#MESSAGE \"Hello world!\"");
        trigger.setAliases(new String[]{"test.alias"});
        trigger.setPermissions(new String[]{"test.permission"});
        trigger.setTabCompleters(new ITabCompleter[]{
                ITabCompleter.Builder.of("abc", "def").build()
        });

        loader.save(trigger);

        verify(mockInfo).put(TriggerConfigKey.KEY_TRIGGER_COMMAND_PERMISSION,
                             new String[]{"test.permission"});
        verify(mockInfo).put(TriggerConfigKey.KEY_TRIGGER_COMMAND_ALIASES,
                             new String[]{"test.alias"});
        //TODO should we save the tab completers? or only to be loaded. Need discussion
    }
}