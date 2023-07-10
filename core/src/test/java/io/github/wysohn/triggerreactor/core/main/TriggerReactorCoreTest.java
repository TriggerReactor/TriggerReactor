package io.github.wysohn.triggerreactor.core.main;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import io.github.wysohn.gsoncopy.JsonElement;
import io.github.wysohn.gsoncopy.JsonParser;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.command.ICommand;
import io.github.wysohn.triggerreactor.core.main.command.ICommandHandler;
import io.github.wysohn.triggerreactor.core.manager.IJavascriptFileLoader;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.ScriptEditManager;
import io.github.wysohn.triggerreactor.core.manager.js.IJSFolderContentCopyHelper;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.ClickTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.WalkTriggerManager;
import io.github.wysohn.triggerreactor.core.module.CorePluginModule;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

public class TriggerReactorCoreTest {

    private static final String COMMAND_NAME = "triggerreactor";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Logger logger;
    private Object pluginObject;

    private IPluginManagement pluginManagement;
    private IGameManagement gameManagement;
    private IEventManagement eventManagement;
    private TaskSupervisor taskSupervisor;

    private ICommandHandler commandHandler;
    private IEventRegistry eventRegistry;
    private IInventoryHandle inventoryHandle;


    private IJSFolderContentCopyHelper copyHelper;
    private IJavascriptFileLoader javascriptFileLoader;

    private SelfReference selfReference;

    @Before
    public void init() throws Exception {
        //initialize with mocks
        logger = mock(Logger.class);
        pluginObject = mock(Object.class);

        pluginManagement = mock(IPluginManagement.class);
        gameManagement = mock(IGameManagement.class);
        eventManagement = mock(IEventManagement.class);
        taskSupervisor = mock(TaskSupervisor.class);

        commandHandler = mock(ICommandHandler.class);
        eventRegistry = mock(IEventRegistry.class);
        inventoryHandle = mock(IInventoryHandle.class);

        copyHelper = mock(IJSFolderContentCopyHelper.class);
        javascriptFileLoader = mock(IJavascriptFileLoader.class);

        selfReference = mock(SelfReference.class);
    }

    private Injector createInjector(AbstractModule... additionalModules) {
        List<AbstractModule> modules = new ArrayList<>();
        modules.add(new AbstractModule() {
                        @Provides
                        @Named("DataFolder")
                        public File providePluginFolder() {
                            return folder.getRoot();
                        }

                        @Provides
                        @Named("PluginLogger")
                        public Logger provideLogger() {
                            return logger;
                        }

                        @Provides
                        @Named("PluginClassLoader")
                        public ClassLoader provideClassLoader() {
                            return getClass().getClassLoader();
                        }

                        @Provides
                        @Named("Plugin")
                        public Object providePluginObject() {
                            return pluginObject;
                        }

                        @Provides
                        public IPluginManagement providePluginManagement() {
                            return pluginManagement;
                        }

                        @Provides
                        public IGameManagement provideGameManagement() {
                            return gameManagement;
                        }

                        @Provides
                        public IEventManagement provideEventManagement() {
                            return eventManagement;
                        }

                        @Provides
                        public TaskSupervisor provideTaskSupervisor() {
                            return taskSupervisor;
                        }

                        @Provides
                        public ICommandHandler provideCommandHandler() {
                            return commandHandler;
                        }

                        @Provides
                        public IEventRegistry provideEventRegistry() {
                            return eventRegistry;
                        }

                        @Provides
                        public IInventoryHandle provideInventoryHandle() {
                            return inventoryHandle;
                        }

                        @Provides
                        public IJSFolderContentCopyHelper provideCopyHelper() {
                            return copyHelper;
                        }

                        @Provides
                        public IJavascriptFileLoader provideJavascriptFileLoader() {
                            return javascriptFileLoader;
                        }

                        @Provides
                        public SelfReference provideSelfReference() {
                            return selfReference;
                        }
                    }
        );
        modules.add(new CorePluginModule());
        modules.addAll(Arrays.asList(additionalModules));

        return Guice.createInjector(modules);
    }

    @Test
    public void initialize() {
        // arrange
        DummyManager dummyManager = new DummyManager();

        TriggerReactorCore triggerReactorCore = createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(DummyManager.class).toInstance(dummyManager);
                    }
                }
        ).getInstance(TriggerReactorCore.class);

        // act
        triggerReactorCore.initialize();

        // assert
        assertTrue(dummyManager.initialized);
    }

    @Test
    public void reload() {
    }

    @Test
    public void shutdown() {
    }

    @Test
    public void command_permissionDenied() {
        // arrange
        ICommandSender sender = mock(ICommandSender.class);

        TRGCommandHandler handler = createInjector().getInstance(TRGCommandHandler.class);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{
                "something"
        });

        // assert
        verify(sender, never()).sendMessage(anyString());
    }

    @Test
    public void command_debug() {
        // arrange
        ICommandSender sender = mock(ICommandSender.class);

        TRGCommandHandler handler = createInjector().getInstance(TRGCommandHandler.class);

        when(sender.hasPermission(TRGCommandHandler.PERMISSION))
                .thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{
                "debug"
        });

        // assert
        verify(pluginManagement).setDebugging(anyBoolean());
    }

    @Test
    public void command_verison() {
        // arrange
        ICommandSender sender = mock(ICommandSender.class);

        TRGCommandHandler handler = createInjector().getInstance(TRGCommandHandler.class);

        when(sender.hasPermission(TRGCommandHandler.PERMISSION))
                .thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(pluginManagement.getVersion()).thenReturn("1.3.4");

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{
                "version"
        });

        // assert
        verify(sender).sendMessage("Current version: 1.3.4");
    }

    @Test
    public void command_clickTrigger_editor() throws IOException {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        IWorld world = mock(IWorld.class, RETURNS_DEEP_STUBS);
        ILocation location = mock(ILocation.class);
        String triggerFolder = "ClickTrigger";
        SimpleLocation simpleLocation = new SimpleLocation("world", 1, 2, 3);

        when(location.getWorld()).thenReturn(world);
        when(sender.getUniqueId()).thenReturn(uuid);
        when(location.toSimpleLocation()).thenReturn(simpleLocation);
        when(world.getBlock(any(ILocation.class)).getTypeName()).thenReturn("STONE");

        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        ScriptEditManager scriptEditManager = injector.getInstance(ScriptEditManager.class);
        ClickTriggerManager clickTriggerManager = injector.getInstance(ClickTriggerManager.class);

        when(sender.hasPermission(TRGCommandHandler.PERMISSION))
                .thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{
                "click"
        });
        boolean result1 = scriptEditManager.isEditing(sender);
        scriptEditManager.onChat(sender, "anystring");
        scriptEditManager.onChat(sender, "#MESSAGE \"Hello World\"");
        scriptEditManager.onChat(sender, "save");
        boolean result2 = scriptEditManager.isEditing(sender);
        clickTriggerManager.handleLocationSetting(location, sender);

        // assert
        assertTrue(result1);
        assertFalse(result2);
        assertNotNull(clickTriggerManager.get(simpleLocation.toString()));

        injector.getInstance(TriggerReactorCore.class).shutdown();
        assertEquals("{}", readContent(triggerFolder, simpleLocation + ".json"));
        assertEquals("#MESSAGE \"Hello World\"", readContent(triggerFolder, simpleLocation + ".trg"));
    }

    @Test
    public void command_clickTrigger_quick() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        IWorld world = mock(IWorld.class, RETURNS_DEEP_STUBS);
        ILocation location = mock(ILocation.class);
        String triggerFolder = "ClickTrigger";
        SimpleLocation simpleLocation = new SimpleLocation("world", 1, 2, 3);

        when(location.getWorld()).thenReturn(world);
        when(sender.getUniqueId()).thenReturn(uuid);
        when(location.toSimpleLocation()).thenReturn(simpleLocation);
        when(world.getBlock(any(ILocation.class)).getTypeName()).thenReturn("STONE");

        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        ClickTriggerManager clickTriggerManager = injector.getInstance(ClickTriggerManager.class);

        when(sender.hasPermission(TRGCommandHandler.PERMISSION))
                .thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{
                "click",
                "#MESSAGE \"Hello World\""
        });
        clickTriggerManager.handleLocationSetting(location, sender);

        // assert
        assertNotNull(clickTriggerManager.get(simpleLocation.toString()));

        injector.getInstance(TriggerReactorCore.class).shutdown();
        assertEquals("{}", readContent(triggerFolder, simpleLocation + ".json"));
        assertEquals("#MESSAGE \"Hello World\"", readContent(triggerFolder, simpleLocation + ".trg"));
    }

    @Test
    public void command_walkTrigger_editor() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        IWorld world = mock(IWorld.class, RETURNS_DEEP_STUBS);
        ILocation location = mock(ILocation.class);
        String triggerFolder = "WalkTrigger";
        SimpleLocation simpleLocation = new SimpleLocation("world", 1, 2, 3);

        when(location.getWorld()).thenReturn(world);
        when(sender.getUniqueId()).thenReturn(uuid);
        when(location.toSimpleLocation()).thenReturn(simpleLocation);
        when(world.getBlock(any(ILocation.class)).getTypeName()).thenReturn("STONE");

        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        ScriptEditManager scriptEditManager = injector.getInstance(ScriptEditManager.class);
        WalkTriggerManager walkTriggerManager = injector.getInstance(WalkTriggerManager.class);

        when(sender.hasPermission(TRGCommandHandler.PERMISSION))
                .thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{
                "walk"
        });
        boolean result1 = scriptEditManager.isEditing(sender);
        scriptEditManager.onChat(sender, "anystring");
        scriptEditManager.onChat(sender, "#MESSAGE \"Hello World\"");
        scriptEditManager.onChat(sender, "save");
        boolean result2 = scriptEditManager.isEditing(sender);
        walkTriggerManager.handleLocationSetting(location, sender);

        // assert
        assertTrue(result1);
        assertFalse(result2);
        assertNotNull(walkTriggerManager.get(simpleLocation.toString()));

        injector.getInstance(TriggerReactorCore.class).shutdown();
        assertEquals("{}", readContent(triggerFolder, simpleLocation + ".json"));
        assertEquals("#MESSAGE \"Hello World\"", readContent(triggerFolder, simpleLocation + ".trg"));
    }

    @Test
    public void command_walkTrigger_quick() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        IWorld world = mock(IWorld.class, RETURNS_DEEP_STUBS);
        ILocation location = mock(ILocation.class);
        String triggerFolder = "WalkTrigger";
        SimpleLocation simpleLocation = new SimpleLocation("world", 1, 2, 3);

        when(location.getWorld()).thenReturn(world);
        when(sender.getUniqueId()).thenReturn(uuid);
        when(location.toSimpleLocation()).thenReturn(simpleLocation);
        when(world.getBlock(any(ILocation.class)).getTypeName()).thenReturn("STONE");

        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        WalkTriggerManager walkTriggerManager = injector.getInstance(WalkTriggerManager.class);

        when(sender.hasPermission(TRGCommandHandler.PERMISSION))
                .thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{
                "walk",
                "#MESSAGE \"Hello World\""
        });
        walkTriggerManager.handleLocationSetting(location, sender);

        // assert
        assertNotNull(walkTriggerManager.get(simpleLocation.toString()));

        injector.getInstance(TriggerReactorCore.class).shutdown();
        assertEquals("{}", readContent(triggerFolder, simpleLocation + ".json"));
        assertEquals("#MESSAGE \"Hello World\"", readContent(triggerFolder, simpleLocation + ".trg"));
    }

    @Test
    public void command_cmdTrigger_editor() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        ICommand mockCommand = mock(ICommand.class);
        String triggerFolder = "CommandTrigger";
        String commandName = "mycmd";

        when(sender.getUniqueId()).thenReturn(uuid);

        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        ScriptEditManager scriptEditManager = injector.getInstance(ScriptEditManager.class);
        CommandTriggerManager cmdTriggerManager = injector.getInstance(CommandTriggerManager.class);

        when(sender.hasPermission(TRGCommandHandler.PERMISSION))
                .thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(commandHandler.register(any(), any())).thenReturn(mockCommand);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{
                "cmd",
                commandName
        });
        boolean result1 = scriptEditManager.isEditing(sender);
        scriptEditManager.onChat(sender, "anystring");
        scriptEditManager.onChat(sender, "#MESSAGE \"Hello World\"");
        scriptEditManager.onChat(sender, "save");
        boolean result2 = scriptEditManager.isEditing(sender);

        // assert
        assertTrue(result1);
        assertFalse(result2);
        assertNotNull(cmdTriggerManager.get("mycmd"));

        injector.getInstance(TriggerReactorCore.class).shutdown();
        assertJsonEquals("{\"aliases\":[], \"permissions\":[]}",
                readContent(triggerFolder, commandName + ".json"));
        assertEquals("#MESSAGE \"Hello World\"",
                readContent(triggerFolder, commandName + ".trg"));
    }

    @Test
    public void command_cmdTrigger_quick() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        ICommand mockCommand = mock(ICommand.class);
        String triggerFolder = "CommandTrigger";
        String commandName = "mycmd";

        when(sender.getUniqueId()).thenReturn(uuid);

        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        CommandTriggerManager cmdTriggerManager = injector.getInstance(CommandTriggerManager.class);

        when(sender.hasPermission(TRGCommandHandler.PERMISSION))
                .thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(commandHandler.register(any(), any())).thenReturn(mockCommand);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{
                "cmd",
                commandName,
                "#MESSAGE \"Hello World\""
        });

        // assert
        assertNotNull(cmdTriggerManager.get("mycmd"));

        injector.getInstance(TriggerReactorCore.class).shutdown();
        assertJsonEquals("{\"aliases\":[], \"permissions\":[]}",
                readContent(triggerFolder, commandName + ".json"));
        assertEquals("#MESSAGE \"Hello World\"",
                readContent(triggerFolder, commandName + ".trg"));
    }

    private void assertJsonEquals(String expected, String actual) {
        JsonParser parser = new JsonParser();
        JsonElement expectedJson = parser.parse(expected);
        JsonElement actualJson = parser.parse(actual);
        assertEquals(expectedJson, actualJson);
    }

    private String readContent(String... paths) throws IOException {
        Path path = Paths.get(folder.getRoot().getAbsolutePath(), paths);
        return new String(Files.readAllBytes(path));
    }

    static class DummyManager extends Manager {
        boolean initialized = false;

        @Override
        public void initialize() {
            initialized = true;
        }

        @Override
        public void reload() {

        }

        @Override
        public void shutdown() {

        }
    }
}