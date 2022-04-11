package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import io.github.wysohn.triggerreactor.components.AreaTriggerTestComponent;
import io.github.wysohn.triggerreactor.components.DaggerAreaTriggerTestComponent;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.main.ThrowableHandler;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineProvider;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.interpreter.IExecutorMap;
import io.github.wysohn.triggerreactor.core.script.interpreter.IPlaceholderMap;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class AreaTriggerTest {
    AreaTriggerTestComponent component;

    @Before
    public void setUp() throws Exception {
        IPluginLifecycleController lifecycleController = mock(IPluginLifecycleController.class);
        IExecutorMap executorMap = mock(IExecutorMap.class);
        IPlaceholderMap placeholderMap = mock(IPlaceholderMap.class);
        IGameController gameController = mock(IGameController.class);
        IScriptEngineProvider scriptEngineProvider = mock(IScriptEngineProvider.class);
        SelfReference selfReference = mock(SelfReference.class);
        TaskSupervisor taskSupervisor = mock(TaskSupervisor.class);

        component = DaggerAreaTriggerTestComponent.builder()
                .pluginLifecycle(lifecycleController)
                .executorMap(executorMap)
                .placeholderMap(placeholderMap)
                .gameController(gameController)
                .scriptEngineProvider(scriptEngineProvider)
                .selfReference(selfReference)
                .taskSupervisor(taskSupervisor)
                .throwableHandler(mock(ThrowableHandler.class))
                .build();
    }

    @Test
    public void testGetArea(){
        TriggerInfo info = mock(TriggerInfo.class);
        File file = mock(File.class);
        IConfigSource configSource = mock(IConfigSource.class);

        when(info.getConfig()).thenReturn(configSource);

        AreaTriggerFactory factory = component.factory();

        AreaTrigger trigger = factory.create(info, file);
        trigger.getArea();

        verify(configSource).get(AreaTrigger.SMALLEST, String.class);
        verify(configSource).get(AreaTrigger.LARGEST, String.class);
    }

    @Test(expected = RuntimeException.class)
    public void testSetAreaDefective() throws Exception {
        TriggerInfo info = mock(TriggerInfo.class);
        File file = mock(File.class);
        IConfigSource configSource = mock(IConfigSource.class);

        when(info.getConfig()).thenReturn(configSource);

        AreaTriggerFactory factory = component.factory();

        AreaTrigger trigger = factory.create(info, file);
        Area area = new Area(new SimpleLocation("world1", 44, 55, 66),
                new SimpleLocation("world2", 88, 99, 100));
        trigger.setArea(area);
    }

    @Test
    public void testSetArea() throws Exception {
        TriggerInfo info = mock(TriggerInfo.class);
        File file = mock(File.class);
        IConfigSource configSource = mock(IConfigSource.class);

        when(info.getConfig()).thenReturn(configSource);

        AreaTriggerFactory factory = component.factory();

        AreaTrigger trigger = factory.create(info, file);
        Area area = new Area(new SimpleLocation("world1", 44, 55, 66),
                new SimpleLocation("world1", 88, 99, 100));
        trigger.setArea(area);
        verify(configSource).put(AreaTrigger.SMALLEST, "world1@44,55,66");
        verify(configSource).put(AreaTrigger.LARGEST, "world1@88,99,100");
    }

    @Test
    public void activate() {
        TriggerInfo info = mock(TriggerInfo.class);
        File file = mock(File.class);
        IConfigSource configSource = mock(IConfigSource.class);
        Map<String, Object> vars = new HashMap<>();

        when(info.getConfig()).thenReturn(configSource);

        AreaTriggerFactory factory = component.factory();

        AreaTrigger.EnterTrigger enterTrigger = mock(AreaTrigger.EnterTrigger.class);
        AreaTrigger.ExitTrigger exitTrigger = mock(AreaTrigger.ExitTrigger.class);
        AreaTrigger trigger = factory.create(info, file);
        trigger.setEnterTrigger(enterTrigger);
        trigger.setExitTrigger(exitTrigger);

        trigger.activate(vars, AreaTriggerManager.EventType.ENTER);
        trigger.activate(vars, AreaTriggerManager.EventType.EXIT);

        verify(enterTrigger).activate(vars);
        verify(exitTrigger).activate(vars);
    }
}