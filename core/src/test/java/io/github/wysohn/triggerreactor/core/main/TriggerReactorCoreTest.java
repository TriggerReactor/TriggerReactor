package io.github.wysohn.triggerreactor.core.main;

import com.google.inject.*;
import com.google.inject.multibindings.ProvidesIntoMap;
import com.google.inject.multibindings.StringMapKey;
import com.google.inject.name.Names;
import io.github.wysohn.gsoncopy.JsonElement;
import io.github.wysohn.gsoncopy.JsonParser;
import io.github.wysohn.triggerreactor.core.Constants;
import io.github.wysohn.triggerreactor.core.bridge.*;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.command.ICommand;
import io.github.wysohn.triggerreactor.core.main.command.ICommandHandler;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.js.IJSFolderContentCopyHelper;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.ClickTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.WalkTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTriggerManager;
import io.github.wysohn.triggerreactor.core.module.CorePluginModule;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
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

        Injector injector = Guice.createInjector(modules);
        injector.getInstance(TriggerReactorCore.class).initialize();
        return injector;
    }

    private String generateLongText(int length) {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < length; i++) {
            longText.append("This is a very long text content. ");
        }
        return longText.toString();
    }

    @Test
    public void initialize_manager() {
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
        triggerReactorCore.shutdown();

        // assert
        assertTrue(dummyManager.initialized);
    }

    private void createDummyInfo(File triggerInfoFile) throws IOException {
        triggerInfoFile.getParentFile().mkdirs();
        if (!triggerInfoFile.exists())
            triggerInfoFile.createNewFile();

        Files.write(triggerInfoFile.toPath(), ("{" +
                "\"first\": 123," +
                "\"second\": \"abc\"," +
                "\"third\": true" +
                "}").getBytes());
    }

    private void verifyDummyInfo(TriggerInfo info) {
        assertEquals(123, (int) info.get(TriggerConfigKey.KEY_TRIGGER_TEST_INTEGER, Integer.class)
                .orElseThrow(RuntimeException::new));
        assertEquals("abc", info.get(TriggerConfigKey.KEY_TRIGGER_TEST_STRING, String.class)
                .orElseThrow(RuntimeException::new));
        assertEquals(true, info.get(TriggerConfigKey.KEY_TRIGGER_TEST_BOOLEAN, Boolean.class)
                .orElseThrow(RuntimeException::new));
    }

    private void createDummyFile(File triggerFile, File triggerInfoFile) throws IOException {
        triggerFile.getParentFile().mkdirs();
        if (!triggerFile.exists())
            triggerFile.createNewFile();

        Files.write(triggerFile.toPath(), "#MESSAGE \"Hello World!\"".getBytes());
        createDummyInfo(triggerInfoFile);
    }

    private void verifyTrigger(Trigger trigger) {
        assertEquals("#MESSAGE \"Hello World!\"", trigger.getScript());
        verifyDummyInfo(trigger.getInfo());
    }

    @Test
    public void initialize_triggerLoaded() throws Exception {
        // arrange
        Injector injector = createInjector();

        File rootFolder = folder.getRoot(); // TriggerReactor

        File clickTriggerFolder = new File(rootFolder, injector.getInstance(Key.get(String.class, Names.named("ClickTriggerManagerFolder"))));
        File clickTriggerFile = new File(clickTriggerFolder, "world@3,5,2.trg");
        File clickTriggerInfoFile = new File(clickTriggerFolder, "world@3,5,2.json");
        createDummyFile(clickTriggerFile, clickTriggerInfoFile);

        File walkTriggerFolder = new File(rootFolder, injector.getInstance(Key.get(String.class, Names.named("WalkTriggerManagerFolder"))));
        File walkTriggerFile = new File(walkTriggerFolder, "world@3,5,2.trg");
        File walkTriggerInfoFile = new File(walkTriggerFolder, "world@3,5,2.json");
        createDummyFile(walkTriggerFile, walkTriggerInfoFile);

        File commandTriggerFolder = new File(rootFolder, injector.getInstance(Key.get(String.class, Names.named("CommandTriggerManagerFolder"))));
        File commandTriggerFile = new File(commandTriggerFolder, "command.trg");
        File commandTriggerInfoFile = new File(commandTriggerFolder, "command.json");
        createDummyFile(commandTriggerFile, commandTriggerInfoFile);

        File areaTriggerFolder = new File(rootFolder, injector.getInstance(Key.get(String.class, Names.named("AreaTriggerManagerFolder"))));
        File areaTriggerFile = new File(areaTriggerFolder, "area"); // area trigger is folder
        File areaTriggerInfoFile = new File(areaTriggerFolder, "area.json");
        File areaTriggerEnterFile = new File(areaTriggerFile, AreaTriggerLoader.NAME_ENTER + ".trg");
        File areaTriggerEnterInfoFile = new File(areaTriggerFile, AreaTriggerLoader.NAME_ENTER + ".json");
        File areaTriggerExitFile = new File(areaTriggerFile, AreaTriggerLoader.NAME_EXIT + ".trg");
        File areaTriggerExitInfoFile = new File(areaTriggerFile, AreaTriggerLoader.NAME_EXIT + ".json");
        createDummyInfo(areaTriggerInfoFile);
        createDummyFile(areaTriggerEnterFile, areaTriggerEnterInfoFile);
        createDummyFile(areaTriggerExitFile, areaTriggerExitInfoFile);

        File namedTriggerFolder = new File(rootFolder, injector.getInstance(Key.get(String.class, Names.named("NamedTriggerManagerFolder"))));
        File namedTriggerFile = new File(namedTriggerFolder, "named.trg");
        File namedTriggerInfoFile = new File(namedTriggerFolder, "named.json");
        createDummyFile(namedTriggerFile, namedTriggerInfoFile);

        File customTriggerFolder = new File(rootFolder, injector.getInstance(Key.get(String.class, Names.named("CustomTriggerManagerFolder"))));
        File customTriggerFile = new File(customTriggerFolder, "custom.trg");
        File customTriggerInfoFile = new File(customTriggerFolder, "custom.json");
        createDummyFile(customTriggerFile, customTriggerInfoFile);
        Files.write(customTriggerInfoFile.toPath(), ("{" +
                "\"first\": 123," +
                "\"second\": \"abc\"," +
                "\"third\": true," +
                "\"event\": \"org.test.event.Event\"" +
                "}").getBytes());

        File inventoryTriggerFolder = new File(rootFolder, injector.getInstance(Key.get(String.class, Names.named("InventoryTriggerManagerFolder"))));
        File inventoryTriggerFile = new File(inventoryTriggerFolder, "inventory.trg");
        File inventoryTriggerInfoFile = new File(inventoryTriggerFolder, "inventory.json");
        createDummyFile(inventoryTriggerFile, inventoryTriggerInfoFile);
        Files.write(inventoryTriggerInfoFile.toPath(), ("{" +
                "\"first\": 123," +
                "\"second\": \"abc\"," +
                "\"third\": true," +
                "\"size\": 18" +
                "}").getBytes());

        File repeatingTriggerFolder = new File(rootFolder, injector.getInstance(Key.get(String.class, Names.named("RepeatingTriggerManagerFolder"))));
        File repeatingTriggerFile = new File(repeatingTriggerFolder, "repeating.trg");
        File repeatingTriggerInfoFile = new File(repeatingTriggerFolder, "repeating.json");
        createDummyFile(repeatingTriggerFile, repeatingTriggerInfoFile);

        TriggerReactorCore triggerReactorCore = injector.getInstance(TriggerReactorCore.class);

        ClickTriggerManager clickTriggerManager = injector.getInstance(ClickTriggerManager.class);
        WalkTriggerManager walkTriggerManager = injector.getInstance(WalkTriggerManager.class);
        CommandTriggerManager commandTriggerManager = injector.getInstance(CommandTriggerManager.class);
        AreaTriggerManager areaTriggerManager = injector.getInstance(AreaTriggerManager.class);
        NamedTriggerManager namedTriggerManager = injector.getInstance(NamedTriggerManager.class);
        CustomTriggerManager customTriggerManager = injector.getInstance(CustomTriggerManager.class);
        InventoryTriggerManager inventoryTriggerManager = injector.getInstance(InventoryTriggerManager.class);
        RepeatingTriggerManager repeatingTriggerManager = injector.getInstance(RepeatingTriggerManager.class);

        when(commandHandler.register(any(), any())).thenReturn(mock(ICommand.class));
        when(javascriptFileLoader.listFiles(any(), any())).thenReturn(new File[0]);
        when(eventRegistry.eventExist(any())).thenReturn(true);
        when(eventRegistry.getEvent(any())).thenReturn((Class) Object.class);

        // act
        triggerReactorCore.initialize();
        triggerReactorCore.shutdown();

        // assert
        assertNotNull(clickTriggerManager.get("world@3,5,2"));
        verifyTrigger(clickTriggerManager.get("world@3,5,2"));
        assertNotNull(walkTriggerManager.get("world@3,5,2"));
        verifyTrigger(walkTriggerManager.get("world@3,5,2"));
        assertNotNull(commandTriggerManager.get("command"));
        verifyTrigger(commandTriggerManager.get("command"));
        assertNotNull(areaTriggerManager.get("area"));
        verifyTrigger(areaTriggerManager.get("area").getEnterTrigger());
        verifyTrigger(areaTriggerManager.get("area").getExitTrigger());
        assertNotNull(namedTriggerManager.get("named"));
        verifyTrigger(namedTriggerManager.get("named"));
        assertNotNull(customTriggerManager.get("custom"));
        verifyTrigger(customTriggerManager.get("custom"));
        assertNotNull(inventoryTriggerManager.get("inventory"));
        verifyTrigger(inventoryTriggerManager.get("inventory"));
        assertNotNull(repeatingTriggerManager.get("repeating"));
        verifyTrigger(repeatingTriggerManager.get("repeating"));
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

        when(sender.hasPermission(Constants.PERMISSION))
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

        when(sender.hasPermission(Constants.PERMISSION))
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

        when(sender.hasPermission(Constants.PERMISSION))
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

        injector.getInstance(TriggerReactorCore.class).shutdown();

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

        when(sender.hasPermission(Constants.PERMISSION))
                .thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{
                "click",
                "#MESSAGE \"Hello World\""
        });
        clickTriggerManager.handleLocationSetting(location, sender);

        injector.getInstance(TriggerReactorCore.class).shutdown();

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

        when(sender.hasPermission(Constants.PERMISSION))
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

        injector.getInstance(TriggerReactorCore.class).shutdown();

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

        when(sender.hasPermission(Constants.PERMISSION))
                .thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{
                "walk",
                "#MESSAGE \"Hello World\""
        });
        walkTriggerManager.handleLocationSetting(location, sender);

        injector.getInstance(TriggerReactorCore.class).shutdown();

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

        when(sender.hasPermission(Constants.PERMISSION))
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

        injector.getInstance(TriggerReactorCore.class).shutdown();

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

        when(sender.hasPermission(Constants.PERMISSION))
                .thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(commandHandler.register(any(), any())).thenReturn(mockCommand);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{
                "cmd",
                commandName,
                "#MESSAGE \"Hello World\""
        });

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertNotNull(cmdTriggerManager.get("mycmd"));

        injector.getInstance(TriggerReactorCore.class).shutdown();
        assertJsonEquals("{\"aliases\":[], \"permissions\":[]}",
                readContent(triggerFolder, commandName + ".json"));
        assertEquals("#MESSAGE \"Hello World\"",
                readContent(triggerFolder, commandName + ".trg"));
    }

    @Test
    public void command_cmdTrigger_sync() throws Exception {
        // arrange
        ICommandSender sender = mock(ICommandSender.class);
        ICommand mockCommand = mock(ICommand.class);

        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        CommandTriggerManager cmdTriggerManager = injector.getInstance(CommandTriggerManager.class);

        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(commandHandler.register(any(), any())).thenReturn(mockCommand);

        cmdTriggerManager.addCommandTrigger(sender, "mycmd", "#MESSAGE \"Hello World\"");

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"cmd", "mycmd", "sync"});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertTrue(cmdTriggerManager.get("mycmd").getInfo().isSync());
    }

    @Test
    public void command_cmdTrigger_permission() throws Exception {
        // arrange
        ICommandSender sender = mock(ICommandSender.class);
        ICommand mockCommand = mock(ICommand.class);

        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        CommandTriggerManager cmdTriggerManager = injector.getInstance(CommandTriggerManager.class);

        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(commandHandler.register(any(), any())).thenReturn(mockCommand);

        cmdTriggerManager.addCommandTrigger(sender, "mycmd", "#MESSAGE \"Hello World\"");

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"cmd", "mycmd", "permission", "my.permission1", "my.permission2", "my.permission3"});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertEquals(Arrays.asList("my.permission1", "my.permission2", "my.permission3"),
                cmdTriggerManager.get("mycmd").getInfo().get(TriggerConfigKey.KEY_TRIGGER_COMMAND_PERMISSION, List.class).get());
    }

    @Test
    public void command_cmdTrigger_alias() throws Exception {
        // arrange
        ICommandSender sender = mock(ICommandSender.class);
        ICommand mockCommand = mock(ICommand.class);

        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        CommandTriggerManager cmdTriggerManager = injector.getInstance(CommandTriggerManager.class);

        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(commandHandler.register(any(), any())).thenReturn(mockCommand);

        cmdTriggerManager.addCommandTrigger(sender, "mycmd", "#MESSAGE \"Hello World\"");

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"cmd", "mycmd", "aliases", "myalias1", "myalias2", "myalias3"});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertEquals(Arrays.asList("myalias1", "myalias2", "myalias3"),
                cmdTriggerManager.get("mycmd").getInfo().get(TriggerConfigKey.KEY_TRIGGER_COMMAND_ALIASES, List.class).get());
    }

    @Test
    public void command_cmdTrigger_tabCompletions() throws Exception {
        //TODO not finalized
        //  track https://github.com/TriggerReactor/TriggerReactor/issues/519
    }

    @Test
    public void command_vars_Item() throws Exception {
        // arrange
        IPlayer sender = mock(IPlayer.class);
        IItemStack itemInHand = mock(IItemStack.class);
        Object sampleItem = "theSample";

        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        GlobalVariableManager globalVariableManager = injector.getInstance(GlobalVariableManager.class);

        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(sender.getItemInMainHand()).thenReturn(itemInHand);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(itemInHand.get()).thenReturn(sampleItem); // this should be a real item, but we don't care for this test

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"vars", "Item", "myItem"});

        // assert
        assertNotNull(globalVariableManager.get("myItem"));
    }

    @Test
    public void command_vars_Location() throws Exception {
        // arrange
        IPlayer sender = mock(IPlayer.class);
        ILocation location = mock(ILocation.class);
        Object sampleLocation = "theSample";

        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        GlobalVariableManager globalVariableManager = injector.getInstance(GlobalVariableManager.class);

        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(sender.getLocation()).thenReturn(location);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(location.get()).thenReturn(sampleLocation); // this should be a real location, but we don't care for this test

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"vars", "Location", "myLocation"});

        // assert
        assertNotNull(globalVariableManager.get("myLocation"));
    }

    @Test
    public void command_vars_KeyValue() throws Exception {
        // arrange
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        GlobalVariableManager globalVariableManager = injector.getInstance(GlobalVariableManager.class);

        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"vars", "myKey", "myValue"});

        // assert
        assertEquals("myValue", globalVariableManager.get("myKey"));
    }

    @Test
    public void command_vars_integer() throws Exception {
        // arrange
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        GlobalVariableManager globalVariableManager = injector.getInstance(GlobalVariableManager.class);

        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"vars", "myKey", "12369587"});

        // assert
        assertEquals(12369587, globalVariableManager.get("myKey"));
    }

    @Test
    public void command_vars_decimal() throws Exception {
        // arrange
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        GlobalVariableManager globalVariableManager = injector.getInstance(GlobalVariableManager.class);

        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"vars", "myKey", "1348.454787"});

        // assert
        assertEquals(1348.454787, globalVariableManager.get("myKey"));
    }

    @Test
    public void command_vars_boolean() {
        // arrange
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        GlobalVariableManager globalVariableManager = injector.getInstance(GlobalVariableManager.class);

        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"vars", "myKey", "false"});

        // assert
        assertEquals(false, globalVariableManager.get("myKey"));
    }

    @Test
    public void command_vars_readValue() {
        // arrange
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        GlobalVariableManager globalVariableManager = injector.getInstance(GlobalVariableManager.class);

        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        globalVariableManager.put("myKey", "myValue3346245");

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"vars", "myKey"});

        // assert
        verify(sender).sendMessage(argThat(message -> message.contains("myValue3346245")));
    }

    @Test
    public void command_test() {
        // arrange
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);

        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"run", "#MESSAGE", "\"Test successful\""});

        // assert
    }

    @Test
    public void command_sudo() {
        // arrange
        IPlayer sender = mock(IPlayer.class);
        IPlayer target = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);

        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(gameManagement.getPlayer("target")).thenReturn(target);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"sudo", "target", "command", "arg1", "arg2"});

        // assert
        verify(eventManagement).createEmptyPlayerEvent(target);
    }

    public void command_namedTrigger_create() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);

        NamedTriggerManager namedTriggerManager = injector.getInstance(NamedTriggerManager.class);
        ScriptEditManager scriptEditManager = injector.getInstance(ScriptEditManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"call", "say_hi", "#MESSAGE", "\"HI\""});
        boolean result1 = scriptEditManager.isEditing(sender);
        scriptEditManager.onChat(sender, "anystring");
        scriptEditManager.onChat(sender, "save");
        boolean result2 = scriptEditManager.isEditing(sender);

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertTrue(result1);
        assertFalse(result2);
        assertNotNull(namedTriggerManager.get("say_hi"));
        assertEquals("#MESSAGE \"HI\"", namedTriggerManager.get("say_hi").getScript());
    }

    @Test
    public void command_namedTrigger_delete() throws Exception {
        // arrange
        File sourceFile = folder.newFile("say_goodbye.trg");

        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        NamedTrigger namedTrigger = mock(NamedTrigger.class);
        TriggerInfo triggerInfo = mock(TriggerInfo.class);

        NamedTriggerManager namedTriggerManager = injector.getInstance(NamedTriggerManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(namedTrigger.getInfo()).thenReturn(triggerInfo);
        when(triggerInfo.getSourceCodeFile()).thenReturn(sourceFile);
        when(namedTrigger.getScript()).thenReturn("#MESSAGE \"Hello World\"");

        namedTriggerManager.put("say_goodbye", namedTrigger);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"del", "call", "say_goodbye"});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertNull(namedTriggerManager.get("say_goodbye"));
    }

    @Test
    public void command_call() throws IOException {
        // arrange
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        NamedTrigger namedTrigger = mock(NamedTrigger.class);
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        File sourceCodeFile = folder.newFile("MyNamedTrigger.trg");

        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(namedTrigger.getInfo()).thenReturn(mockInfo);
        when(namedTrigger.getScript()).thenReturn("#MESSAGE \"Hello World\"");
        when(mockInfo.getSourceCodeFile()).thenReturn(sourceCodeFile);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(taskSupervisor.submitSync(any())).thenAnswer(invocation -> {
            Callable callable = invocation.getArgument(0);
            callable.call();
            return CompletableFuture.completedFuture(null);
        });

        NamedTriggerManager namedTriggerManager = injector.getInstance(NamedTriggerManager.class);

        namedTriggerManager.put("MyNamedTrigger", namedTrigger);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"call", "MyNamedTrigger", "arg=123;arg2=\"456\""});

        // assert
        Map<String, Object> expectedArgs = new HashMap<>();
        expectedArgs.put("arg", 123);
        expectedArgs.put("arg2", "456");

        verify(namedTrigger).activate(any(), argThat(otherMap -> {
            expectedArgs.forEach((key, value) -> assertEquals(value, otherMap.get(key)));
            return true;
        }));
    }

    @Test
    public void command_invTrigger_create() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);

        InventoryTriggerManager inventoryTriggerManager = injector.getInstance(InventoryTriggerManager.class);
        ScriptEditManager scriptEditManager = injector.getInstance(ScriptEditManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"i", "MyInventory", "create", "54"});
        boolean result1 = scriptEditManager.isEditing(sender);
        scriptEditManager.onChat(sender, "anystring");
        scriptEditManager.onChat(sender, "#MESSAGE \"Hello World\"");
        scriptEditManager.onChat(sender, "save");
        boolean result2 = scriptEditManager.isEditing(sender);

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertTrue(result1);
        assertFalse(result2);
        assertNotNull(inventoryTriggerManager.get("MyInventory"));
        assertEquals(54, inventoryTriggerManager.get("MyInventory").getItems().length);
        assertEquals("#MESSAGE \"Hello World\"", inventoryTriggerManager.get("MyInventory").getScript());
    }

    @Test
    public void command_invTrigger_quick() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);

        InventoryTriggerManager inventoryTriggerManager = injector.getInstance(InventoryTriggerManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"i", "MyInventory", "create", "54", "#MESSAGE \"Hello World\""});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertNotNull(inventoryTriggerManager.get("MyInventory"));
        assertEquals(54, inventoryTriggerManager.get("MyInventory").getItems().length);
        assertEquals("#MESSAGE \"Hello World\"", inventoryTriggerManager.get("MyInventory").getScript());
    }

    @Test
    public void command_invTrigger_settitle() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);

        InventoryTriggerManager inventoryTriggerManager = injector.getInstance(InventoryTriggerManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"i", "MyInventory", "create", "54", "#MESSAGE \"Hello World\""});
        handler.onCommand(sender, COMMAND_NAME, new String[]{"i", "MyInventory", "settitle", "Custom_Title"});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertNotNull(inventoryTriggerManager.get("MyInventory"));
        assertEquals(54, inventoryTriggerManager.get("MyInventory").getItems().length);
        assertEquals("#MESSAGE \"Hello World\"", inventoryTriggerManager.get("MyInventory").getScript());
        assertEquals("Custom_Title", inventoryTriggerManager.get("MyInventory").getTitle());
    }

    @Test
    public void command_invTrigger_delete() throws Exception {
        // arrange
        File sourceFile = folder.newFile("MyInventory.trg");

        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        InventoryTrigger inventoryTrigger = mock(InventoryTrigger.class);
        TriggerInfo triggerInfo = mock(TriggerInfo.class);

        InventoryTriggerManager inventoryTriggerManager = injector.getInstance(InventoryTriggerManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(inventoryTrigger.getInfo()).thenReturn(triggerInfo);
        when(triggerInfo.getSourceCodeFile()).thenReturn(sourceFile);
        when(inventoryTrigger.getScript()).thenReturn("#MESSAGE \"Hello World\"");
        when(inventoryTrigger.getItems()).thenReturn(new IItemStack[54]);

        inventoryTriggerManager.put("MyInventory", inventoryTrigger);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"i", "MyInventory", "delete"});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertNull(inventoryTriggerManager.get("MyInventory"));
    }

    @Test
    public void command_invTrigger_item() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        IItemStack itemInHand = mock(IItemStack.class);

        InventoryTriggerManager inventoryTriggerManager = injector.getInstance(InventoryTriggerManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(sender.getItemInMainHand()).thenReturn(itemInHand);
        when(itemInHand.getType()).thenReturn("STONE"); // should be Material.STONE, but not necessary for test
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(itemInHand.clone()).thenReturn(itemInHand);

        inventoryTriggerManager.createTrigger(54, "MyInventory", "#MESSAGE \"Hello World\"");

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"i", "MyInventory", "item", "1"});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertNotNull(inventoryTriggerManager.get("MyInventory"));
        assertEquals(54, inventoryTriggerManager.get("MyInventory").getItems().length);
        assertEquals("#MESSAGE \"Hello World\"", inventoryTriggerManager.get("MyInventory").getScript());
        assertEquals("STONE", inventoryTriggerManager.get("MyInventory").getItems()[0].getType());
    }

    @Test
    public void command_invTrigger_createPickup() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);

        InventoryTriggerManager inventoryTriggerManager = injector.getInstance(InventoryTriggerManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"i", "MyInventory", "create", "54", "#MESSAGE \"Hello World\""});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertNotNull(inventoryTriggerManager.get("MyInventory"));
        assertFalse(inventoryTriggerManager.get("MyInventory").canPickup());
    }

    @Test
    public void command_invTrigger_canPickupItem() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);

        InventoryTriggerManager inventoryTriggerManager = injector.getInstance(InventoryTriggerManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"i", "MyInventory", "create", "54", "#MESSAGE \"Hello World\""});
        handler.onCommand(sender, COMMAND_NAME, new String[]{"i", "MyInventory", "pickup"});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertNotNull(inventoryTriggerManager.get("MyInventory"));
        assertTrue(inventoryTriggerManager.get("MyInventory").canPickup());
    }

    @Test
    public void command_invTrigger_open() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        IItemStack itemInHand = mock(IItemStack.class);
        IInventory inventory = mock(IInventory.class);

        InventoryTriggerManager inventoryTriggerManager = injector.getInstance(InventoryTriggerManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(sender.getItemInMainHand()).thenReturn(itemInHand);
        when(itemInHand.getType()).thenReturn("STONE"); // should be Material.STONE, but not necessary for test
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(itemInHand.clone()).thenReturn(itemInHand);
        when(inventoryHandle.createInventory(anyInt(), any())).thenReturn(inventory);
        when(inventory.get()).thenReturn(new Object());

        inventoryTriggerManager.createTrigger(54, "MyInventory", "#MESSAGE \"Hello World\"");

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"i", "MyInventory", "open"});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        verify(sender).openInventory(any(IInventory.class));
    }

    @Test
    public void command_areaTrigger_create() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);

        AreaTriggerManager areaTriggerManager = injector.getInstance(AreaTriggerManager.class);
        AreaSelectionManager areaSelectionManager = injector.getInstance(AreaSelectionManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"a", "toggle"});
        boolean result1 = areaSelectionManager.isSelecting(uuid);
        areaSelectionManager.onClick(AreaSelectionManager.ClickAction.LEFT_CLICK_BLOCK,
                uuid,
                new SimpleLocation("world", 0, 0, 0));
        areaSelectionManager.onClick(AreaSelectionManager.ClickAction.RIGHT_CLICK_BLOCK,
                uuid,
                new SimpleLocation("world", 10, 10, 10));
        handler.onCommand(sender, COMMAND_NAME, new String[]{"a", "MyArea", "create"});
        boolean result2 = areaSelectionManager.isSelecting(uuid);

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertTrue(result1);
        assertFalse(result2);
        assertNotNull(areaTriggerManager.get("MyArea"));
        assertEquals("world", areaTriggerManager.get("MyArea").getArea().getSmallest().getWorld());
        assertEquals(0, areaTriggerManager.get("MyArea").getArea().getSmallest().getX());
        assertEquals(0, areaTriggerManager.get("MyArea").getArea().getSmallest().getY());
        assertEquals(0, areaTriggerManager.get("MyArea").getArea().getSmallest().getZ());
        assertEquals("world", areaTriggerManager.get("MyArea").getArea().getLargest().getWorld());
        assertEquals(10, areaTriggerManager.get("MyArea").getArea().getLargest().getX());
        assertEquals(10, areaTriggerManager.get("MyArea").getArea().getLargest().getY());
        assertEquals(10, areaTriggerManager.get("MyArea").getArea().getLargest().getZ());
    }

    @Test
    public void command_areaTrigger_delete() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        AreaTrigger areaTrigger = mock(AreaTrigger.class);
        TriggerInfo triggerInfo = mock(TriggerInfo.class);

        AreaTriggerManager areaTriggerManager = injector.getInstance(AreaTriggerManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(areaTrigger.getInfo()).thenReturn(triggerInfo);
        when(triggerInfo.getSourceCodeFile()).thenReturn(folder.newFile("MyArea.trg"));
        when(areaTrigger.getScript()).thenReturn("#MESSAGE \"Hello World\"");

        areaTriggerManager.createArea("MyArea",
                new SimpleLocation("world", 0, 0, 0),
                new SimpleLocation("world", 10, 10, 10));
        boolean exists = areaTriggerManager.get("MyArea") != null;

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"a", "MyArea", "delete"});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertTrue(exists);
        assertNull(areaTriggerManager.get("MyArea"));
    }

    @Test
    public void command_areaTrigger_enter_quick() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        AreaTrigger areaTrigger = mock(AreaTrigger.class);
        TriggerInfo triggerInfo = mock(TriggerInfo.class);

        AreaTriggerManager areaTriggerManager = injector.getInstance(AreaTriggerManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(areaTrigger.getInfo()).thenReturn(triggerInfo);
        when(triggerInfo.getSourceCodeFile()).thenReturn(folder.newFile("MyArea.trg"));
        when(areaTrigger.getScript()).thenReturn("#MESSAGE \"Hello World\"");

        areaTriggerManager.createArea("MyArea",
                new SimpleLocation("world", 0, 0, 0),
                new SimpleLocation("world", 10, 10, 10));

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"a", "MyArea", "enter", "#MESSAGE \"Hello World\""});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        AreaTrigger myArea = areaTriggerManager.get("MyArea");
        assertNotNull(myArea);
        assertEquals("#MESSAGE \"Hello World\"", myArea.getEnterTrigger().getScript());
    }

    @Test
    public void command_areaTrigger_exit_quick() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        AreaTrigger areaTrigger = mock(AreaTrigger.class);
        TriggerInfo triggerInfo = mock(TriggerInfo.class);

        AreaTriggerManager areaTriggerManager = injector.getInstance(AreaTriggerManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(areaTrigger.getInfo()).thenReturn(triggerInfo);
        when(triggerInfo.getSourceCodeFile()).thenReturn(folder.newFile("MyArea.trg"));
        when(areaTrigger.getScript()).thenReturn("#MESSAGE \"Hello World\"");

        areaTriggerManager.createArea("MyArea",
                new SimpleLocation("world", 0, 0, 0),
                new SimpleLocation("world", 10, 10, 10));

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"a", "MyArea", "exit", "#MESSAGE \"Hello World\""});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        AreaTrigger myArea = areaTriggerManager.get("MyArea");
        assertNotNull(myArea);
        assertEquals("#MESSAGE \"Hello World\"", myArea.getExitTrigger().getScript());
    }

    @Test
    public void command_customTrigger_create() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        CustomTrigger customTrigger = mock(CustomTrigger.class);
        TriggerInfo triggerInfo = mock(TriggerInfo.class);

        CustomTriggerManager customTriggerManager = injector.getInstance(CustomTriggerManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(customTrigger.getInfo()).thenReturn(triggerInfo);
        when(triggerInfo.getSourceCodeFile()).thenReturn(folder.newFile("MyCustomTrigger.trg"));
        when(customTrigger.getScript()).thenReturn("#MESSAGE \"Hello World\"");
        doReturn(Object.class).when(eventRegistry).getEvent("onJoin");

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"custom", "onJoin", "MyCustomTrigger", "#MESSAGE \"Hello World\""});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertNotNull(customTriggerManager.get("MyCustomTrigger"));
        assertEquals("#MESSAGE \"Hello World\"", customTriggerManager.get("MyCustomTrigger").getScript());
    }

    @Test
    public void command_customTrigger_setPriority() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        CustomTrigger customTrigger = mock(CustomTrigger.class);
        TriggerInfo triggerInfo = mock(TriggerInfo.class);

        CustomTriggerManager customTriggerManager = injector.getInstance(CustomTriggerManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(customTrigger.getInfo()).thenReturn(triggerInfo);
        when(triggerInfo.getSourceCodeFile()).thenReturn(folder.newFile("MyCustomTrigger.trg"));
        when(customTrigger.getScript()).thenReturn("#MESSAGE \"Hello World\"");
        doReturn(Object.class).when(eventRegistry).getEvent("onJoin");

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"custom", "onJoin", "MyCustomTrigger", "#MESSAGE \"Hello World\""});
        handler.onCommand(sender, COMMAND_NAME, new String[]{"priority", "MyCustomTrigger", "normal"});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertNotNull(customTriggerManager.get("MyCustomTrigger"));
        assertEquals("#MESSAGE \"Hello World\"", customTriggerManager.get("MyCustomTrigger").getScript());
        assertEquals(300, customTriggerManager.get("MyCustomTrigger").getPriority());
    }

    @Test
    public void command_repeatTrigger_create() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);

        RepeatingTriggerManager repeatingTriggerManager = injector.getInstance(RepeatingTriggerManager.class);
        ScriptEditManager scriptEditManager = injector.getInstance(ScriptEditManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"r", "MyRepeat"});
        boolean result1 = scriptEditManager.isEditing(sender);
        scriptEditManager.onChat(sender, "anystring");
        scriptEditManager.onChat(sender, "#MESSAGE \"Hello World\"");
        scriptEditManager.onChat(sender, "save");
        boolean result2 = scriptEditManager.isEditing(sender);

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertTrue(result1);
        assertFalse(result2);
        assertNotNull(repeatingTriggerManager.get("MyRepeat"));
        assertEquals("#MESSAGE \"Hello World\"", repeatingTriggerManager.get("MyRepeat").getScript());
    }

    @Test
    public void command_repeatTrigger_reload() throws Exception {
        // arrange
        UUID uuid = UUID.randomUUID();
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);

        RepeatingTriggerManager repeatingTriggerManager = injector.getInstance(RepeatingTriggerManager.class);
        ScriptEditManager scriptEditManager = injector.getInstance(ScriptEditManager.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(pluginManagement.getConsoleSender()).thenReturn(sender);

        when(javascriptFileLoader.listFiles(any(), any())).thenReturn(new File[0]);
        when(taskSupervisor.isServerThread()).thenReturn(true);
        when(taskSupervisor.submitSync(any(Callable.class))).thenAnswer(invocation -> {
            Callable callable = invocation.getArgument(0);
            return CompletableFuture.completedFuture(callable.call());
        });
        when(taskSupervisor.newThread(any(), any(), anyInt())).thenAnswer(invocation ->
                new Thread((Runnable) invocation.getArgument(0)));
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(taskSupervisor).runTask(any(Runnable.class));

        // act
        for (int i = 0; i < 150; i++) {
            handler.onCommand(sender, COMMAND_NAME, new String[]{"r", "MyRepeat" + i});
            scriptEditManager.onChat(sender, "anystring");
            scriptEditManager.onChat(sender, "#MESSAGE \"Hello World\"" + i + ";");
            scriptEditManager.onChat(sender, "save");
            handler.onCommand(sender, COMMAND_NAME, new String[]{"r", "MyRepeat" + i, "interval", "1t"});
        }

        for (int i = 0; i < 10; i++)
            handler.onCommand(sender, COMMAND_NAME, new String[]{"reload", "confirm"});

        repeatingTriggerManager.shutdown();
        // assert

    }

    @Test
    public void command_saveAll_globalVariable() throws Exception {
        // arrange
        int max = 100000;
        Injector injector = createInjector();

        IPlayer sender = mock(IPlayer.class);

        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        GlobalVariableManager globalVariableManager = injector.getInstance(GlobalVariableManager.class);

        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        for (int i = 0; i < max; i++) {
            globalVariableManager.put("key" + i, "value" + i);
            if (i % 1000 == 0) {
                handler.onCommand(sender, COMMAND_NAME, new String[]{"saveAll"});
                handler.onCommand(sender, COMMAND_NAME, new String[]{"reload", "confirm"});
            }
        }

        handler.onCommand(sender, COMMAND_NAME, new String[]{"saveAll"});
        handler.onCommand(sender, COMMAND_NAME, new String[]{"reload", "confirm"});

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertJsonEquals(generateSampleJson(max), readContent("var.json"));
    }

    @Test
    public void command_saveAll_commandTrigger() throws Exception {
        // arrange
        String longText = generateLongText(100000);

        int max = 1000;
        Injector injector = createInjector();

        CommandTriggerManager commandTriggerManager = injector.getInstance(CommandTriggerManager.class);

        TriggerInfo info = mock(TriggerInfo.class);
        when(info.getSourceCodeFile()).thenReturn(folder.newFile("command.json"));
        CommandTrigger trigger = mock(CommandTrigger.class);
        when(trigger.getInfo()).thenReturn(info);
        when(trigger.getScript()).thenReturn(longText);
        when(trigger.getAliases()).thenReturn(new String[0]);

        commandTriggerManager.put("command", trigger);

        // act
        for (int i = 0; i < max; i++) {
            commandTriggerManager.saveAll();
            commandTriggerManager.reload();
        }

        injector.getInstance(TriggerReactorCore.class).shutdown();

        // assert
        assertEquals(longText, readContent("command.json"));
    }

    @Test
    public void inventoryTrigger_shares_variable() throws Exception {
        // arrange
        Executor testExecutor = mock(Executor.class);

        Injector injector = createInjector(new AbstractModule() {
            @ProvidesIntoMap
            @StringMapKey("TEST")
            public Executor testExecutor() {
                return testExecutor;
            }
        });

        IPlayer sender = mock(IPlayer.class);
        IInventory platformInventory = mock(IInventory.class);
        IItemStack clickedItem = mock(IItemStack.class);

        InventoryTriggerManager inventoryTriggerManager = injector.getInstance(InventoryTriggerManager.class);

        when(sender.hasPermission(Constants.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(inventoryHandle.createInventory(anyInt(), any())).thenReturn(platformInventory);
        when(platformInventory.get()).thenReturn(new Object());
        when(sender.get()).thenReturn(new Object());
        when(clickedItem.clone()).thenReturn(clickedItem);
        when(clickedItem.get()).thenReturn(new Object());
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(taskSupervisor).submitAsync(any());

        // act
        inventoryTriggerManager.createTrigger(54, "test", "" +
                "IF trigger==\"open\";" +
                "    myvar = 17;" +
                "ENDIF;" +
                ";" +
                "IF trigger==\"click\";" +
                "    #TEST myvar;" +
                "ENDIF;");
        IInventory ref = inventoryTriggerManager.openGUI(sender, "test");
        inventoryTriggerManager.onOpen(
                new Object(),
                ref,
                sender
        );
        inventoryTriggerManager.onClick(
                new Object(),
                ref,
                clickedItem,
                0,
                "LEFT",
                -1,
                (b) -> {
                });

        // assert
        assertNotNull(ref);
        verify(testExecutor).evaluate(any(), any(), any(), eq(17));
    }

    private String generateSampleJson(int size) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (int i = 0; i < size; i++) {
            builder.append("\"key").append(i).append("\":\"value").append(i).append("\"");
            if (i != size - 1)
                builder.append(",");
        }
        builder.append("}");
        return builder.toString();
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
