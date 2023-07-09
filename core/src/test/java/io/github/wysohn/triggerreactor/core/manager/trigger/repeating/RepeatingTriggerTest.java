package io.github.wysohn.triggerreactor.core.manager.trigger.repeating;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.manager.IGlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.js.IBackedMapProvider;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.core.script.interpreter.Placeholder;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RepeatingTriggerTest {
    IGlobalVariableManager globalVariableManager;
    IBackedMapProvider<Executor> executorManager;
    IBackedMapProvider<Placeholder> placeholderManager;
    IExceptionHandle exceptionHandle;
    TaskSupervisor taskSupervisor;
    IPluginManagement pluginManagement;
    SelfReference selfReference;

    IRepeatingTriggerFactory factory;

    @Before
    public void init() throws IllegalAccessException, NoSuchFieldException {
        globalVariableManager = mock(IGlobalVariableManager.class);
        executorManager = mock(IBackedMapProvider.class);
        placeholderManager = mock(IBackedMapProvider.class);
        exceptionHandle = mock(IExceptionHandle.class);
        taskSupervisor = mock(TaskSupervisor.class);
        pluginManagement = mock(IPluginManagement.class);
        selfReference = mock(SelfReference.class);

        factory = Guice.createInjector(
                new FactoryModuleBuilder()
                        .implement(RepeatingTrigger.class, RepeatingTrigger.class)
                        .build(IRepeatingTriggerFactory.class),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(IGlobalVariableManager.class).toInstance(globalVariableManager);
                        bind(new TypeLiteral<IBackedMapProvider<Executor>>() {
                        })
                                .toInstance(executorManager);
                        bind(new TypeLiteral<IBackedMapProvider<Placeholder>>() {
                        })
                                .toInstance(placeholderManager);
                        bind(new TypeLiteral<Map<String, Class<? extends AbstractAPISupport>>>() {
                        })
                                .toInstance(new HashMap<>());
                        bind(IExceptionHandle.class).toInstance(exceptionHandle);
                        bind(TaskSupervisor.class).toInstance(taskSupervisor);
                        bind(IPluginManagement.class).toInstance(pluginManagement);
                        bind(SelfReference.class).toInstance(selfReference);
                    }
                }
        ).getInstance(IRepeatingTriggerFactory.class);
    }

    @Test
    public void run() throws InterruptedException {
        // arrange
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        String script = "script";

        RepeatingTrigger repeatingTrigger = factory.create(mockInfo, script);

        Future mockFuture = mock(Future.class);
        when(taskSupervisor.submitSync(any()))
                .thenAnswer(invocationOnMock -> {
                    Callable callable = invocationOnMock.getArgument(0);
                    callable.call();
                    return mockFuture;
                });

        // act
        repeatingTrigger.activate(null, new HashMap<>(), true);
        Thread thread = new Thread(repeatingTrigger::run);
        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                thread.interrupt();
            }
        });

        thread2.start();
        thread.start();

        thread.join();

        // assert
    }
}