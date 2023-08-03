package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerDependencyFacade;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterGlobalContext;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AreaTriggerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private ITriggerDependencyFacade dependencyFacade;
    private TaskSupervisor taskSupervisor;
    private IExceptionHandle exceptionHandle;
    private InterpreterGlobalContext globalContext;
    private SelfReference selfReference;

    private IAreaTriggerFactory factory;

    @Before
    public void init() {
        dependencyFacade = mock(ITriggerDependencyFacade.class);
        taskSupervisor = mock(TaskSupervisor.class);
        exceptionHandle = mock(IExceptionHandle.class);
        globalContext = mock(InterpreterGlobalContext.class);
        selfReference = mock(SelfReference.class);

        factory = Guice.createInjector(
                new FactoryModuleBuilder()
                        .implement(AreaTrigger.class, AreaTrigger.class)
                        .build(IAreaTriggerFactory.class),
                new FactoryModuleBuilder()
                        .implement(EnterTrigger.class, EnterTrigger.class)
                        .build(IEnterTriggerFactory.class),
                new FactoryModuleBuilder()
                        .implement(ExitTrigger.class, ExitTrigger.class)
                        .build(IExitTriggerFactory.class),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(ITriggerDependencyFacade.class).toInstance(dependencyFacade);
                        bind(TaskSupervisor.class).toInstance(taskSupervisor);
                        bind(IExceptionHandle.class).toInstance(exceptionHandle);
                        bind(InterpreterGlobalContext.class).toInstance(globalContext);
                        bind(SelfReference.class).toInstance(selfReference);
                    }
                }
        ).getInstance(IAreaTriggerFactory.class);
    }

    @Test
    public void getEntity() {
        // Arrange
        TriggerInfo info = mock(TriggerInfo.class);
        Area area = new Area(
                new SimpleLocation("world", 0, 0, 0),
                new SimpleLocation("world", 16, 16, 16)
        );

        File triggerFolder = temporaryFolder.getRoot();
        AreaTrigger areaTrigger = factory.create(info, area, triggerFolder);

        UUID uuid = UUID.randomUUID();
        IEntity entity = mock(IEntity.class);

        when(entity.getUniqueId()).thenReturn(uuid);

        // Act
        areaTrigger.addEntity(entity);
        IEntity result = areaTrigger.getEntity(uuid);

        // Assert
        assertEquals(entity, result);
    }

    @Test
    public void getEntities() {
        // Arrange
        TriggerInfo info = mock(TriggerInfo.class);
        Area area = new Area(
                new SimpleLocation("world", 0, 0, 0),
                new SimpleLocation("world", 16, 16, 16)
        );

        File triggerFolder = temporaryFolder.getRoot();
        AreaTrigger areaTrigger = factory.create(info, area, triggerFolder);

        for (int i = 0; i < 1000; i++) {
            IEntity entity = mock(IEntity.class);
            when(entity.getUniqueId()).thenReturn(UUID.randomUUID());
            areaTrigger.addEntity(entity);
        }
        System.gc(); // TODO not sure this is even reliable way to test this

        // Act
        List<IEntity> result = areaTrigger.getEntities();

        // Assert
        assertTrue(result.size() < 1000); // entity is de-referenced as it is created, so it should be less than 1000
    }
}