package io.github.wysohn.triggerreactor.core.main;

import io.github.wysohn.triggerreactor.components.DaggerTriggerReactorMainTestComponent;
import io.github.wysohn.triggerreactor.components.TriggerReactorMainTestComponent;
import io.github.wysohn.triggerreactor.core.components.PluginMainComponent;
import io.github.wysohn.triggerreactor.core.manager.IInventoryModifier;
import io.github.wysohn.triggerreactor.core.manager.IResourceProvider;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandMapHandler;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.IEventRegistry;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.IGUIOpenHelper;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptEngineManager;
import java.io.File;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

public class TriggerReactorTest {

    PluginMainComponent mainComponent;

    @Before
    public void init(){
        File dataFolder = new File("build/tmp/test");

        TriggerReactorMainTestComponent testComponent = DaggerTriggerReactorMainTestComponent.builder()
                .commandMapHandler(mock(ICommandMapHandler.class))
                .taskSupervisor(mock(TaskSupervisor.class))
                .dataFolder(dataFolder)
                .eventRegistry(mock(IEventRegistry.class))
                .gameController(mock(IGameController.class))
                .guiOpenHandler(mock(IGUIOpenHelper.class))
                .inventoryModifier(mock(IInventoryModifier.class))
                .itemStackClass(ItemStack.class)
                .logger(mock(Logger.class))
                .pluginInstance(mock(Object.class))
                .pluginLifecycle(mock(IPluginLifecycleController.class))
                .resourceManager(mock(IResourceProvider.class))
                .scriptEngineManager(new ScriptEngineManager())
                .selfReference(mock(SelfReference.class))
                .wrapper(mock(IWrapper.class))
                .commandName("test")
                .permission("test")
                .build();

        mainComponent = testComponent.getMainBuilder().build();
    }

    @Test
    public void onEnable() throws Exception {
        TriggerReactor main = mainComponent.getMain();

        main.onEnable();
    }

    public static class ItemStack{

    }
}