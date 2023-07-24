package io.github.wysohn.triggerreactor.core.main;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import io.github.wysohn.gsoncopy.JsonElement;
import io.github.wysohn.gsoncopy.JsonParser;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.command.ICommand;
import io.github.wysohn.triggerreactor.core.main.command.ICommandHandler;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.js.IJSFolderContentCopyHelper;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
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

    @Test
    public void command_cmdTrigger_sync() throws Exception {
        // arrange
        ICommandSender sender = mock(ICommandSender.class);
        ICommand mockCommand = mock(ICommand.class);

        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        CommandTriggerManager cmdTriggerManager = injector.getInstance(CommandTriggerManager.class);

        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(commandHandler.register(any(), any())).thenReturn(mockCommand);

        cmdTriggerManager.addCommandTrigger(sender, "mycmd", "#MESSAGE \"Hello World\"");

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"cmd", "mycmd", "sync"});

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

        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(commandHandler.register(any(), any())).thenReturn(mockCommand);

        cmdTriggerManager.addCommandTrigger(sender, "mycmd", "#MESSAGE \"Hello World\"");

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"cmd", "mycmd", "permission", "my.permission1", "my.permission2", "my.permission3"});

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

        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(commandHandler.register(any(), any())).thenReturn(mockCommand);

        cmdTriggerManager.addCommandTrigger(sender, "mycmd", "#MESSAGE \"Hello World\"");

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"cmd", "mycmd", "aliases", "myalias1", "myalias2", "myalias3"});

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

        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
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

        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
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

        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
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

        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
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

        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
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

        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
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

        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
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

        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"run", "#MESSAGE", "\"Test successful\""});

        // assert
    }

    @Test
    public void command_call() {
        // arrange
        IPlayer sender = mock(IPlayer.class);
        Injector injector = createInjector();
        TRGCommandHandler handler = injector.getInstance(TRGCommandHandler.class);
        NamedTrigger namedTrigger = mock(NamedTrigger.class);

        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
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
        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"i", "MyInventory", "create", "54"});
        boolean result1 = scriptEditManager.isEditing(sender);
        scriptEditManager.onChat(sender, "anystring");
        scriptEditManager.onChat(sender, "#MESSAGE \"Hello World\"");
        scriptEditManager.onChat(sender, "save");
        boolean result2 = scriptEditManager.isEditing(sender);

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
        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"i", "MyInventory", "create", "54", "#MESSAGE \"Hello World\""});

        // assert
        assertNotNull(inventoryTriggerManager.get("MyInventory"));
        assertEquals(54, inventoryTriggerManager.get("MyInventory").getItems().length);
        assertEquals("#MESSAGE \"Hello World\"", inventoryTriggerManager.get("MyInventory").getScript());
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
        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(inventoryTrigger.getInfo()).thenReturn(triggerInfo);
        when(triggerInfo.getSourceCodeFile()).thenReturn(sourceFile);
        when(inventoryTrigger.getScript()).thenReturn("#MESSAGE \"Hello World\"");
        when(inventoryTrigger.getItems()).thenReturn(new IItemStack[54]);

        inventoryTriggerManager.put("MyInventory", inventoryTrigger);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"i", "MyInventory", "delete"});

        // assert
        assertNull(inventoryTriggerManager.get("MyInventory"));
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
        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
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
        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
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
        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(areaTrigger.getInfo()).thenReturn(triggerInfo);
        when(triggerInfo.getSourceCodeFile()).thenReturn(folder.newFile("MyArea.trg"));
        when(areaTrigger.getScript()).thenReturn("#MESSAGE \"Hello World\"");

        areaTriggerManager.createArea("MyArea",
                new SimpleLocation("world", 0, 0, 0),
                new SimpleLocation("world", 10, 10, 10));

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"a", "MyArea", "enter", "#MESSAGE \"Hello World\""});

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
        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(areaTrigger.getInfo()).thenReturn(triggerInfo);
        when(triggerInfo.getSourceCodeFile()).thenReturn(folder.newFile("MyArea.trg"));
        when(areaTrigger.getScript()).thenReturn("#MESSAGE \"Hello World\"");

        areaTriggerManager.createArea("MyArea",
                new SimpleLocation("world", 0, 0, 0),
                new SimpleLocation("world", 10, 10, 10));

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"a", "MyArea", "exit", "#MESSAGE \"Hello World\""});

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
        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);
        when(customTrigger.getInfo()).thenReturn(triggerInfo);
        when(triggerInfo.getSourceCodeFile()).thenReturn(folder.newFile("MyCustomTrigger.trg"));
        when(customTrigger.getScript()).thenReturn("#MESSAGE \"Hello World\"");
        doReturn(Object.class).when(eventRegistry).getEvent("onJoin");

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"custom", "onJoin", "MyCustomTrigger", "#MESSAGE \"Hello World\""});

        // assert
        assertNotNull(customTriggerManager.get("MyCustomTrigger"));
        assertEquals("#MESSAGE \"Hello World\"", customTriggerManager.get("MyCustomTrigger").getScript());
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
        when(sender.hasPermission(TRGCommandHandler.PERMISSION)).thenReturn(true);
        when(pluginManagement.isEnabled()).thenReturn(true);

        // act
        handler.onCommand(sender, COMMAND_NAME, new String[]{"r", "MyRepeat"});
        boolean result1 = scriptEditManager.isEditing(sender);
        scriptEditManager.onChat(sender, "anystring");
        scriptEditManager.onChat(sender, "#MESSAGE \"Hello World\"");
        scriptEditManager.onChat(sender, "save");
        boolean result2 = scriptEditManager.isEditing(sender);

        // assert
        assertTrue(result1);
        assertFalse(result2);
        assertNotNull(repeatingTriggerManager.get("MyRepeat"));
        assertEquals("#MESSAGE \"Hello World\"", repeatingTriggerManager.get("MyRepeat").getScript());
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