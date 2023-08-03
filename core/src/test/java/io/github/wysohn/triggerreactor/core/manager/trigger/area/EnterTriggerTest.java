package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.manager.IGlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.IJavascriptFileLoader;
import io.github.wysohn.triggerreactor.core.manager.js.IBackedMapProvider;
import io.github.wysohn.triggerreactor.core.manager.js.IJSFolderContentCopyHelper;
import io.github.wysohn.triggerreactor.core.manager.js.IScriptEngineGateway;
import io.github.wysohn.triggerreactor.core.manager.js.executor.IJSExecutorFactory;
import io.github.wysohn.triggerreactor.core.manager.js.placeholder.IJSPlaceholderFactory;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerDependencyFacade;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.module.TestFileModule;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.Placeholder;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class EnterTriggerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    IGlobalVariableManager globalVariableManager;
    IBackedMapProvider<Executor> executorMapProvider;
    IBackedMapProvider<Placeholder> placeholderMapProvider;
    IPluginManagement pluginManagement;
    TaskSupervisor taskSupervisor;
    SelfReference selfReference;
    ITriggerDependencyFacade dependencyFacade;

    IEnterTriggerFactory factory;

    @Before
    public void setUp() throws Exception {
        globalVariableManager = mock(IGlobalVariableManager.class);
        executorMapProvider = mock(IBackedMapProvider.class);
        placeholderMapProvider = mock(IBackedMapProvider.class);
        dependencyFacade = mock(ITriggerDependencyFacade.class);

        factory = Guice.createInjector(
                new FactoryModuleBuilder()
                        .implement(EnterTrigger.class, EnterTrigger.class)
                        .build(IEnterTriggerFactory.class),
                new TestFileModule(folder),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(IGlobalVariableManager.class).toInstance(globalVariableManager);
                        bind(new TypeLiteral<IBackedMapProvider<Executor>>() {
                        }).toInstance(executorMapProvider);
                        bind(new TypeLiteral<IBackedMapProvider<Placeholder>>() {
                        }).toInstance(placeholderMapProvider);
                        bind(IPluginManagement.class).toInstance(mock(IPluginManagement.class));
                        bind(TaskSupervisor.class).toInstance(mock(TaskSupervisor.class));
                        bind(SelfReference.class).toInstance(mock(SelfReference.class));

                        bind(new TypeLiteral<Map<String, Executor>>() {
                        })
                                .toInstance(new HashMap<>());
                        bind(new TypeLiteral<Map<String, Placeholder>>() {
                        })
                                .toInstance(new HashMap<>());
                        bind(new TypeLiteral<Set<IScriptEngineGateway>>() {
                        })
                                .toInstance(new HashSet<>());
                        bind(new TypeLiteral<Map<String, Class<? extends AbstractAPISupport>>>() {
                        })
                                .toInstance(new HashMap<>());
                        bind(IJSFolderContentCopyHelper.class).toInstance(mock(IJSFolderContentCopyHelper.class));
                        bind(IJSExecutorFactory.class).toInstance(mock(IJSExecutorFactory.class));
                        bind(IJSPlaceholderFactory.class).toInstance(mock(IJSPlaceholderFactory.class));
                        bind(IJavascriptFileLoader.class).toInstance(mock(IJavascriptFileLoader.class));
                        bind(IExceptionHandle.class).toInstance(mock(IExceptionHandle.class));
                        bind(ITriggerDependencyFacade.class).toInstance(dependencyFacade);
                    }
                }
        ).getInstance(IEnterTriggerFactory.class);
    }

    private void assertFieldsEqual(EnterTrigger trigger1, EnterTrigger trigger2) throws IllegalAccessException {
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(EnterTrigger.class.getDeclaredFields()));
        fields.addAll(Arrays.asList(Trigger.class.getDeclaredFields()));
        for (Field field : fields) {
            // TODO figure out the way to copy the interpreter as it is
            //  when cloning, the interpreter is not copied, but rather re-initialized,
            //  yet ideally, it should be copied as it is, so we don't have to re-read
            //  the script file and generate the AST again.
            if (field.getType() == Interpreter.class)
                continue;

            field.setAccessible(true);
            assertEquals(field.get(trigger1), field.get(trigger2));
        }
    }

    @Test
    public void testClone() throws IllegalAccessException, AbstractTriggerManager.TriggerInitFailedException {
        // arrange
        TriggerInfo info = mock(TriggerInfo.class);
        String script = "script";
        AreaTrigger areaTrigger = mock(AreaTrigger.class);

        // act
        EnterTrigger trigger = factory.create(info, script, areaTrigger);
        trigger.init();
        EnterTrigger clone = (EnterTrigger) trigger.clone();
        clone.init();

        // assert
        assertFieldsEqual(trigger, clone);
    }
}