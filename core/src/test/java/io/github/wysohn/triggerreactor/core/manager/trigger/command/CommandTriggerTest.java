package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.manager.ExternalAPIManager;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineProvider;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterGlobalContext;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.tools.observer.IObserver;
import io.github.wysohn.triggerreactor.tools.test.WhiteBox;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CommandTriggerTest {

    ExternalAPIManager apiManager;
    InterpreterGlobalContext globalContext;
    IGameController gameController;
    TaskSupervisor taskSupervisor;
    IThrowableHandler throwableHandler;
    IScriptEngineProvider scriptEngineProvider;
    Map<String, DynamicTabCompleter> dynamicTabCompleterMap = new HashMap<>();

    CommandTriggerFactory factory;

    @Before
    public void setUp() throws Exception {
        apiManager = mock(ExternalAPIManager.class);
        globalContext = mock(InterpreterGlobalContext.class);
        gameController = mock(IGameController.class);
        taskSupervisor = mock(TaskSupervisor.class);
        throwableHandler = mock(IThrowableHandler.class);
        scriptEngineProvider = mock(IScriptEngineProvider.class);

        factory = DaggerCommandTriggerTestComponent.builder()
                .externalAPI(apiManager)
                .globalContext(globalContext)
                .gameController(gameController)
                .taskSupervisor(taskSupervisor)
                .throwableHandler(throwableHandler)
                .scriptEngineProvider(scriptEngineProvider)
                .tabCompleterMap(dynamicTabCompleterMap)
                .build()
                .factory();
    }

    @Test
    public void testConstructor() throws Exception {
        TriggerInfo info = mock(TriggerInfo.class);
        IConfigSource config = mock(IConfigSource.class);

        when(info.getConfig()).thenReturn(config);

        CommandTrigger trigger = factory.create(info, "test");
    }

    @Test
    public void testCompleterLoading() throws Exception {
        TriggerInfo info = mock(TriggerInfo.class);
        IConfigSource config = mock(IConfigSource.class);
        DynamicTabCompleter playerlist = mock(DynamicTabCompleter.class);

        when(info.getConfig()).thenReturn(config);
        when(config.get(CommandTrigger.TABCOMPLETER, List.class))
                .thenReturn(Optional.of(Arrays.asList("test1", "test2,test5,test6", "$playerlist")));
        dynamicTabCompleterMap.put("playerlist", playerlist);

        CommandTrigger trigger = factory.create(info, "test");

        assertEquals(3, trigger.getTabCompleters().length);
        assertSame(StaticTabCompleter.class, trigger.getTabCompleters()[0].getClass());
        assertSame(StaticTabCompleter.class, trigger.getTabCompleters()[1].getClass());
        assertSame(DynamicTabCompleter.class, trigger.getTabCompleters()[2].getClass());
    }

    @Test
    public void testGetPermissions(){
        TriggerInfo info = mock(TriggerInfo.class);
        IConfigSource config = mock(IConfigSource.class);

        when(info.getConfig()).thenReturn(config);
        when(config.has(CommandTrigger.PERMISSION)).thenReturn(true);
        when(config.get(CommandTrigger.PERMISSION, List.class)).thenReturn(Optional.of(Arrays.asList("testpermission1", "testpermission2")));

        CommandTrigger trigger = factory.create(info, "test");

        assertArrayEquals(new String[]{"testpermission1", "testpermission2"}, trigger.getPermissions());
    }

    @Test
    public void testGetPermissions_Empty(){
        TriggerInfo info = mock(TriggerInfo.class);
        IConfigSource config = mock(IConfigSource.class);

        when(info.getConfig()).thenReturn(config);
        when(config.has(CommandTrigger.PERMISSION)).thenReturn(false);

        CommandTrigger trigger = factory.create(info, "test");

        assertArrayEquals(new String[]{}, trigger.getPermissions());
    }

    @Test
    public void testSetPermissions() throws Exception {
        TriggerInfo info = mock(TriggerInfo.class);
        IConfigSource config = mock(IConfigSource.class);
        IObserver observer = mock(IObserver.class);

        when(info.getConfig()).thenReturn(config);
        when(config.has(CommandTrigger.PERMISSION)).thenReturn(true);

        CommandTrigger trigger = factory.create(info, "test");
        WhiteBox.setInternalState(trigger, "observer", observer);
        trigger.setPermissions(new String[]{"testpermission3", "testpermission4"});

        verify(config).put(eq(CommandTrigger.PERMISSION), eq(new String[]{"testpermission3", "testpermission4"}));
        verify(observer).onUpdate(eq(trigger));
    }

    @Test
    public void testGetAliases(){
        TriggerInfo info = mock(TriggerInfo.class);
        IConfigSource config = mock(IConfigSource.class);

        when(info.getConfig()).thenReturn(config);
        when(config.has(CommandTrigger.ALIASES)).thenReturn(true);
        when(config.get(CommandTrigger.ALIASES, List.class)).thenReturn(Optional.of(Arrays.asList("testalias1", "testalias2")));

        CommandTrigger trigger = factory.create(info, "test");

        assertArrayEquals(new String[]{"testalias1", "testalias2"}, trigger.getAliases());
    }

    @Test
    public void testGetAliases_Empty(){
        TriggerInfo info = mock(TriggerInfo.class);
        IConfigSource config = mock(IConfigSource.class);

        when(info.getConfig()).thenReturn(config);
        when(config.has(CommandTrigger.ALIASES)).thenReturn(false);

        CommandTrigger trigger = factory.create(info, "test");

        assertArrayEquals(new String[]{}, trigger.getAliases());
    }

    @Test
    public void testSetAliases() throws Exception {
        TriggerInfo info = mock(TriggerInfo.class);
        IConfigSource config = mock(IConfigSource.class);
        IObserver observer = mock(IObserver.class);

        when(info.getConfig()).thenReturn(config);
        when(config.has(CommandTrigger.ALIASES)).thenReturn(true);

        CommandTrigger trigger = factory.create(info, "test");
        WhiteBox.setInternalState(trigger, "observer", observer);
        trigger.setAliases(new String[]{"testalias3", "testalias4"});

        verify(config).put(eq(CommandTrigger.ALIASES), eq(new String[]{"testalias3", "testalias4"}));
        verify(observer).onUpdate(eq(trigger));
    }

    @Test
    public void testGetCompleter(){
        TriggerInfo info = mock(TriggerInfo.class);
        IConfigSource config = mock(IConfigSource.class);

        when(info.getConfig()).thenReturn(config);
        when(config.has(CommandTrigger.TABCOMPLETER)).thenReturn(true);
        when(config.get(CommandTrigger.TABCOMPLETER, List.class)).thenReturn(Optional.of(Arrays.asList("testcompleter1", "testcompleter2")));

        CommandTrigger trigger = factory.create(info, "test");

        assertEquals(StaticTabCompleter.class, trigger.getTabCompleters()[0].getClass());
        assertEquals(StaticTabCompleter.class, trigger.getTabCompleters()[1].getClass());
    }

    @Test
    public void testSetCompleter() throws Exception {
        TriggerInfo info = mock(TriggerInfo.class);
        IConfigSource config = mock(IConfigSource.class);
        IObserver observer = mock(IObserver.class);

        when(info.getConfig()).thenReturn(config);
        when(config.has(CommandTrigger.TABCOMPLETER)).thenReturn(true);

        CommandTrigger trigger = factory.create(info, "test");
        WhiteBox.setInternalState(trigger, "observer", observer);
        trigger.setTabCompleters(new ITabCompleter[]{StaticTabCompleter.Builder.of("aa", "bb").build(),
                                                     StaticTabCompleter.Builder.of("cc", "dd").build(),
                                                     DynamicTabCompleter.Builder.of("playerlist",
                                                             () -> Arrays.asList("wysohn", "wysohn2"))
                                                             .build()});

        verify(config).put(eq(CommandTrigger.TABCOMPLETER), eq(new String[]{"aa,bb", "cc,dd", "playerlist"}));
        verify(observer).onUpdate(eq(trigger));
    }

    @Test
    public void testGetSync(){
        TriggerInfo info = mock(TriggerInfo.class);
        IConfigSource config = mock(IConfigSource.class);

        when(info.isSync()).thenReturn(true);
        when(info.getConfig()).thenReturn(config);
        when(config.has(TriggerInfo.KEY_SYNC)).thenReturn(true);

        CommandTrigger trigger = factory.create(info, "test");

        assertTrue(trigger.isSync());
    }

    @Test
    public void testGetNotSync(){
        TriggerInfo info = mock(TriggerInfo.class);
        IConfigSource config = mock(IConfigSource.class);

        when(info.isSync()).thenReturn(false);
        when(info.getConfig()).thenReturn(config);
        when(config.has(TriggerInfo.KEY_SYNC)).thenReturn(true);

        CommandTrigger trigger = factory.create(info, "test");

        assertFalse(trigger.isSync());
    }
}