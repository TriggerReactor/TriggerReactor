package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.components.DaggerTriggerCommandTestComponent;
import io.github.wysohn.triggerreactor.components.TriggerCommandTestComponent;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.selection.AreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.selection.LocationSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.click.ClickTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.walk.WalkTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTriggerManager;
import io.github.wysohn.triggerreactor.tools.TimeUtil;
import io.github.wysohn.triggerreactor.tools.timings.Timings;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TriggerCommandTest {
    IPluginLifecycleController pluginLifecycleController = mock(IPluginLifecycleController.class);
    IGameController gameController = mock(IGameController.class);
    IInventoryModifier inventoryModifier = mock(IInventoryModifier.class);
    IThrowableHandler throwableHandler = mock(IThrowableHandler.class);

    ScriptEditManager editManager = mock(ScriptEditManager.class);
    GlobalVariableManager variableManager = mock(GlobalVariableManager.class);
    InventoryEditManager inventoryEditManager = mock(InventoryEditManager.class);
    AreaSelectionManager areaSelectionManager = mock(AreaSelectionManager.class);
    ExecutorManager executorManager = mock(ExecutorManager.class);
    PlaceholderManager placeholderManager = mock(PlaceholderManager.class);
    LocationSelectionManager locationSelectionManager = mock(LocationSelectionManager.class);

    ClickTriggerManager clickTriggerManager = mock(ClickTriggerManager.class);
    WalkTriggerManager walkTriggerManager = mock(WalkTriggerManager.class);
    CommandTriggerManager commandTriggerManager = mock(CommandTriggerManager.class);
    NamedTriggerManager namedTriggerManager = mock(NamedTriggerManager.class);
    InventoryTriggerManager inventoryTriggerManager = mock(InventoryTriggerManager.class);
    AreaTriggerManager areaTriggerManager = mock(AreaTriggerManager.class);
    CustomTriggerManager customTriggerManager = mock(CustomTriggerManager.class);
    RepeatingTriggerManager repeatingTriggerManager = mock(RepeatingTriggerManager.class);

    TriggerCommandTestComponent.Builder builder;

    private IPlayer player;
    private Function<SimpleLocation, Boolean> fn;
    private Consumer<String> saveHandler;

    @Before
    public void setUp() throws Exception {
        Manager.ACTIVE_MANAGERS.clear();

        builder = DaggerTriggerCommandTestComponent.builder()
                .pluginLifecycle(pluginLifecycleController)
                .gameController(gameController)
                .throwableHandler(throwableHandler)
                .inventoryModifier(inventoryModifier)
                .manager(editManager)
                .manager(variableManager)
                .manager(inventoryEditManager)
                .manager(areaSelectionManager)
                .manager(executorManager)
                .manager(placeholderManager)
                .manager(locationSelectionManager)
                .manager(clickTriggerManager)
                .manager(walkTriggerManager)
                .manager(commandTriggerManager)
                .manager(namedTriggerManager)
                .manager(inventoryTriggerManager)
                .manager(areaTriggerManager)
                .manager(customTriggerManager)
                .manager(repeatingTriggerManager);
    }

    @Test
    public void printHierarchy() {
        TriggerCommand triggerCommand = new TriggerCommand();
        ITriggerCommand command = triggerCommand.createCommand();
        ICommandSender sender = mock(ICommandSender.class);
        triggerCommand.pluginLifecycleController = mock(IPluginLifecycleController.class);

        doAnswer(invocation -> {
            System.out.println((String) invocation.getArgument(0));
            return null;
        }).when(sender).sendMessage(anyString());

        command.printUsage(sender, 999);
    }

    @Test
    public void testClickTriggerPlayerOnly() {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        command.onCommand(sender, ITriggerCommand.toQueue("c"));

        verify(locationSelectionManager, never()).startLocationSet(any(), any());
    }

    @Test
    public void testClickTriggerLocationSetBegin() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        command.onCommand(sender, ITriggerCommand.toQueue("c"));

        verify(locationSelectionManager).startLocationSet(eq(sender), any());
    }

    @Test
    public void testClickTriggerLocationClicked() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        SimpleLocation clickedLoc = new SimpleLocation("World", 4, 55, 22);

        when(locationSelectionManager.startLocationSet(any(), any())).then(invocation -> {
            player = invocation.getArgument(0);
            fn = invocation.getArgument(1);
            return true;
        });
        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());

        command.onCommand(sender, ITriggerCommand.toQueue("c"));
        fn.apply(clickedLoc);
        saveHandler.accept("some edits");

        assertEquals(player, sender);
        verify(locationSelectionManager).startLocationSet(eq(sender), any());
        verify(editManager).startEdit(eq(sender), anyString(), eq(""), eq(saveHandler), eq(false));
        verify(clickTriggerManager).put(clickedLoc, "some edits");
    }

    @Test
    public void testClickTriggerLocationClickedDuplicate() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        SimpleLocation clickedLoc = new SimpleLocation("World", 4, 55, 22);
        Trigger trigger = mock(Trigger.class);

        when(locationSelectionManager.startLocationSet(any(), any())).then(invocation -> {
            player = invocation.getArgument(0);
            fn = invocation.getArgument(1);
            return true;
        });
        when(clickTriggerManager.get(clickedLoc)).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("c"));
        assertFalse(fn.apply(clickedLoc));
        assertFalse(fn.apply(clickedLoc));
        assertFalse(fn.apply(clickedLoc));

        assertEquals(player, sender);
        verify(locationSelectionManager).startLocationSet(eq(sender), any());
        verify(clickTriggerManager, times(3)).showTriggerInfo(eq(sender), eq(clickedLoc));
    }

    @Test
    public void testClickTriggerInProgress() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        when(locationSelectionManager.startLocationSet(any(), any())).then(invocation -> false);

        command.onCommand(sender, ITriggerCommand.toQueue("c"));

        verify(sender).sendMessage("&cSelection already in progress.");
    }

    @Test
    public void testWalkTriggerPlayerOnly() {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        command.onCommand(sender, ITriggerCommand.toQueue("w"));

        verify(locationSelectionManager, never()).startLocationSet(any(), any());
    }

    @Test
    public void testWalkTriggerLocationClicked() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        SimpleLocation clickedLoc = new SimpleLocation("World", 8, 12, -88);

        when(locationSelectionManager.startLocationSet(any(), any())).then(invocation -> {
            player = invocation.getArgument(0);
            fn = invocation.getArgument(1);
            return true;
        });
        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());

        command.onCommand(sender, ITriggerCommand.toQueue("w"));
        fn.apply(clickedLoc);
        saveHandler.accept("other edits");

        assertEquals(player, sender);
        verify(locationSelectionManager).startLocationSet(eq(sender), any());
        verify(editManager).startEdit(eq(sender), anyString(), eq(""), eq(saveHandler), eq(false));
        verify(walkTriggerManager).put(clickedLoc, "other edits");
    }

    @Test
    public void testWalkTriggerLocationClickedDuplicate() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        SimpleLocation clickedLoc = new SimpleLocation("World", 8, 12, -88);
        Trigger trigger = mock(Trigger.class);

        when(locationSelectionManager.startLocationSet(any(), any())).then(invocation -> {
            player = invocation.getArgument(0);
            fn = invocation.getArgument(1);
            return true;
        });
        when(walkTriggerManager.get(clickedLoc)).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("w"));
        assertFalse(fn.apply(clickedLoc));
        assertFalse(fn.apply(clickedLoc));
        assertFalse(fn.apply(clickedLoc));

        assertEquals(player, sender);
        verify(locationSelectionManager).startLocationSet(eq(sender), any());
        verify(walkTriggerManager, times(3)).showTriggerInfo(eq(sender), eq(clickedLoc));
    }

    @Test
    public void testWalkTriggerInProgress() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        when(locationSelectionManager.startLocationSet(any(), any())).then(invocation -> false);

        command.onCommand(sender, ITriggerCommand.toQueue("w"));

        verify(sender).sendMessage("&cSelection already in progress.");
    }

    @Test
    public void testSearchPlayerOnly() {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        command.onCommand(sender, ITriggerCommand.toQueue("search"));

        verify(gameController, never()).showGlowStones(eq(sender), anySet());
    }

    @Test
    public void testSearch() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        command.onCommand(sender, ITriggerCommand.toQueue("search"));

        verify(clickTriggerManager).getTriggersInChunk(any());
        verify(walkTriggerManager).getTriggersInChunk(any());
        verify(gameController, times(2)).showGlowStones(eq(sender), anySet());
    }

    @Test
    public void testCommandTriggerNew() {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());

        command.onCommand(sender, ITriggerCommand.toQueue("cmd new MyCommand #MESSAGE \"Hi\""));
        saveHandler.accept("#MESSAGE \"Hi\"");

        verify(commandTriggerManager).createCommandTrigger(eq("MyCommand"), eq("#MESSAGE \"Hi\""));
        verify(editManager).startEdit(any(), anyString(), eq("#MESSAGE \"Hi\""), eq(saveHandler), eq(true));
    }

    @Test
    public void testCommandTriggerNewAlreadyExist() {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        when(commandTriggerManager.has(anyString())).thenReturn(true);
        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());

        command.onCommand(sender, ITriggerCommand.toQueue("cmd new MyCommand #MESSAGE \"Hi\""));

        verify(commandTriggerManager, never()).createCommandTrigger(eq("MyCommand"), eq("#MESSAGE \"Hi\""));
        verify(editManager, never()).startEdit(any(), anyString(), eq("#MESSAGE \"Hi\" "), eq(saveHandler), eq(true));
    }

    @Test
    public void testCommandTriggerEdit() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);
        CommandTrigger trigger = mock(CommandTrigger.class);

        when(commandTriggerManager.has(anyString())).thenReturn(true);
        when(commandTriggerManager.get(eq("MyCommand"))).thenReturn(trigger);
        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());
        when(trigger.getScript()).thenReturn("");

        command.onCommand(sender, ITriggerCommand.toQueue("cmd edit MyCommand"));
        saveHandler.accept("#MESSAGE \"Hello\"");

        verify(trigger).setScript(eq("#MESSAGE \"Hello\""));
    }

    @Test
    public void testCommandTriggerEditNotExist() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        when(commandTriggerManager.has(anyString())).thenReturn(false);

        command.onCommand(sender, ITriggerCommand.toQueue("cmd edit MyCommand"));

        verify(sender).sendMessage(eq("&cCommand not found."));
    }

    @Test
    public void testCommandTriggerEditScriptError() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);
        CommandTrigger trigger = mock(CommandTrigger.class);

        when(commandTriggerManager.has(anyString())).thenReturn(true);
        when(commandTriggerManager.get(eq("MyCommand"))).thenReturn(trigger);
        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());
        when(trigger.getScript()).thenReturn("");
        doThrow(new AbstractTriggerManager.TriggerInitFailedException("", null)).when(trigger).setScript(anyString());

        command.onCommand(sender, ITriggerCommand.toQueue("cmd edit MyCommand"));
        saveHandler.accept("#MESSAGE \"Hello\"");

        verify(trigger).setScript(eq("#MESSAGE \"Hello\""));
        verify(throwableHandler).handleException(eq(sender),
                any(AbstractTriggerManager.TriggerInitFailedException.class));
    }

    @Test
    public void testCommandTriggerDelete() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);
        CommandTrigger trigger = mock(CommandTrigger.class);

        when(commandTriggerManager.remove(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("cmd del MyCommand"));

        verify(sender).sendMessage("&aCommand is deleted.");
    }

    @Test
    public void testCommandTriggerDeleteNotExist() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        when(commandTriggerManager.remove(anyString())).thenReturn(null);

        command.onCommand(sender, ITriggerCommand.toQueue("cmd del MyCommand"));

        verify(sender).sendMessage("&cCommand not found.");
    }

    @Test
    public void testCommandTriggerSync() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);
        CommandTrigger trigger = mock(CommandTrigger.class);

        when(commandTriggerManager.has(anyString())).thenReturn(true);
        when(commandTriggerManager.get(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("cmd sync MyCommand"));

        verify(trigger).setSync(true);
    }

    @Test
    public void testCommandTriggerPermission() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);
        CommandTrigger trigger = mock(CommandTrigger.class);

        when(commandTriggerManager.has(anyString())).thenReturn(true);
        when(commandTriggerManager.get(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("cmd p MyCommand x.y x.z y.y"));

        verify(trigger).setPermissions(eq(new String[]{"x.y", "x.z", "y.y"}));
    }

    @Test
    public void testCommandTriggerPermissionDelete() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);
        CommandTrigger trigger = mock(CommandTrigger.class);
        TriggerInfo info = mock(TriggerInfo.class);

        when(commandTriggerManager.has(anyString())).thenReturn(true);
        when(commandTriggerManager.get(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("cmd p MyCommand"));

        verify(trigger).setPermissions(eq(null));
    }

    @Test
    public void testCommandTriggerAliases() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);
        CommandTrigger trigger = mock(CommandTrigger.class);
        TriggerInfo info = mock(TriggerInfo.class);

        when(commandTriggerManager.has(anyString())).thenReturn(true);
        when(commandTriggerManager.get(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("cmd a MyCommand alias1 alias2"));

        verify(trigger).setAliases(eq(new String[]{"alias1", "alias2"}));
        verify(commandTriggerManager).reregisterCommand(eq("MyCommand"));
    }

    @Test
    public void testCommandTriggerAliasesDelete() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);
        CommandTrigger trigger = mock(CommandTrigger.class);
        TriggerInfo info = mock(TriggerInfo.class);

        when(commandTriggerManager.has(anyString())).thenReturn(true);
        when(commandTriggerManager.get(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("cmd a MyCommand"));

        verify(trigger).setAliases(eq(null));
        verify(commandTriggerManager).reregisterCommand(eq("MyCommand"));
    }

    @Test
    public void testCommandTriggerTabs() {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);
        CommandTrigger trigger = mock(CommandTrigger.class);

        when(commandTriggerManager.has(anyString())).thenReturn(true);
        when(commandTriggerManager.get(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("cmd tab MyCommand aaa,bb,ccc $playerlist"));

        List<String> expected = new LinkedList<>();
        expected.add("aaa,bb,ccc");
        expected.add("$playerlist");
        verify(trigger).setTabCompleters(eq(expected));
        verify(commandTriggerManager).reload(eq("MyCommand"));
    }

    @Test
    public void testInventoryTriggerCreate() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());

        command.onCommand(sender, ITriggerCommand.toQueue("i create SomeInv 54"));
        saveHandler.accept("some script");

        verify(inventoryTriggerManager).createTrigger(eq(54), eq("SomeInv"), eq("some script"));
    }

    @Test
    public void testInventoryTriggerEdit() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);
        InventoryTrigger trigger = mock(InventoryTrigger.class);

        when(trigger.getScript()).thenReturn("");
        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());
        when(inventoryTriggerManager.has(anyString())).thenReturn(true);
        when(inventoryTriggerManager.get(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("i edit SomeInv"));
        saveHandler.accept("some edits");

        verify(trigger).setScript(eq("some edits"));
    }

    @Test
    public void testInventoryTriggerDelete() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);
        InventoryTrigger trigger = mock(InventoryTrigger.class);

        when(inventoryTriggerManager.remove(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("i del MyCommand"));

        verify(sender).sendMessage("&aDeleted!");
    }

    @Test
    public void testInventoryTriggerItem() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        InventoryTrigger trigger = mock(InventoryTrigger.class);
        IItemStack item = mock(IItemStack.class);

        when(sender.getItemInMainHand()).thenReturn(item);
        when(inventoryTriggerManager.get(anyString())).thenReturn(trigger);
        when(item.clone()).thenReturn(item);
        when(trigger.size()).thenReturn(54);

        command.onCommand(sender, ITriggerCommand.toQueue("i item MyCommand 22"));

        verify(trigger).setItem(eq(item), eq(21));
    }

    @Test
    public void testInventoryTriggerColumn() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        InventoryTrigger trigger = mock(InventoryTrigger.class);
        IItemStack item = mock(IItemStack.class);

        when(sender.getItemInMainHand()).thenReturn(item);
        when(inventoryTriggerManager.get(anyString())).thenReturn(trigger);
        when(item.clone()).thenReturn(item);
        when(trigger.size()).thenReturn(54);

        command.onCommand(sender, ITriggerCommand.toQueue("i column MyCommand 2"));

        verify(trigger).setColumn(eq(item), eq(1));
    }

    @Test
    public void testInventoryTriggerRow() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        InventoryTrigger trigger = mock(InventoryTrigger.class);
        IItemStack item = mock(IItemStack.class);

        when(sender.getItemInMainHand()).thenReturn(item);
        when(inventoryTriggerManager.get(anyString())).thenReturn(trigger);
        when(item.clone()).thenReturn(item);
        when(trigger.size()).thenReturn(54);

        command.onCommand(sender, ITriggerCommand.toQueue("i row MyCommand 6"));

        verify(trigger).setRow(eq(item), eq(5));
    }

    @Test
    public void testInventoryTriggerOpen() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        command.onCommand(sender, ITriggerCommand.toQueue("i open MyCommand"));

        verify(inventoryTriggerManager).openGUI(eq(sender), eq("MyCommand"));
    }

    @Test
    public void testInventoryTriggerOpenOther() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        IPlayer other = mock(IPlayer.class);

        when(gameController.getPlayer(eq("wysohn"))).thenReturn(other);

        command.onCommand(sender, ITriggerCommand.toQueue("i open MyCommand wysohn"));

        verify(inventoryTriggerManager).openGUI(eq(other), eq("MyCommand"));
    }

    @Test
    public void testInventoryTriggerEditItems() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        InventoryTrigger trigger = mock(InventoryTrigger.class);

        when(inventoryTriggerManager.get(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("i edititems MyCommand"));

        verify(inventoryEditManager).onStartEdit(eq(sender), eq(trigger));
    }

    @Test
    public void testInventoryTriggerSetTitle() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        InventoryTrigger trigger = mock(InventoryTrigger.class);

        when(inventoryTriggerManager.get(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("i settitle MyCommand newTitle"));

        verify(trigger).setTitle(eq("newTitle"));
    }

    @Test
    public void testAreaTriggerToggle() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();

        when(sender.getUniqueId()).thenReturn(uuid);

        command.onCommand(sender, ITriggerCommand.toQueue("a toggle"));

        verify(areaSelectionManager).toggleSelection(eq(uuid));
    }

    @Test
    public void testAreaTriggerCreate() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();
        SimpleLocation from = new SimpleLocation("world", 0, 0, 0);
        SimpleLocation to = new SimpleLocation("world", 50, 50, 50);
        Area area = new Area(from, to);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(areaSelectionManager.getSelection(eq(uuid))).thenReturn(area);
        when(areaTriggerManager.getConflictingAreas(area, area::equals)).thenReturn(new HashSet<>());
        when(areaTriggerManager.createArea(anyString(), any(), any())).thenReturn(true);

        command.onCommand(sender, ITriggerCommand.toQueue("a create SomeArea"));

        verify(areaTriggerManager).createArea(eq("SomeArea"), eq(from), eq(to));
        verify(areaSelectionManager).resetSelections(eq(uuid));
    }

    @Test
    public void testAreaTriggerDelete() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();
        AreaTrigger area = mock(AreaTrigger.class);

        when(sender.getUniqueId()).thenReturn(uuid);
        when(areaTriggerManager.remove(eq("SomeArea"))).thenReturn(area);

        command.onCommand(sender, ITriggerCommand.toQueue("a delete SomeArea"));

        verify(areaTriggerManager).remove("SomeArea");
        verify(areaSelectionManager).resetSelections(eq(uuid));
    }

    @Test
    public void testAreaTriggerEnter() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        AreaTrigger areaTrigger = mock(AreaTrigger.class);

        when(areaTriggerManager.get(eq("SomeArea"))).thenReturn(areaTrigger);
        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());

        command.onCommand(sender, ITriggerCommand.toQueue("a enter SomeArea"));
        saveHandler.accept("edits");

        verify(areaTrigger).setEnterTrigger(eq("edits"));
    }

    @Test
    public void testAreaTriggerExit() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        AreaTrigger areaTrigger = mock(AreaTrigger.class);

        when(areaTriggerManager.get(eq("SomeArea"))).thenReturn(areaTrigger);
        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());

        command.onCommand(sender, ITriggerCommand.toQueue("a exit SomeArea"));
        saveHandler.accept("edits2");

        verify(areaTrigger).setExitTrigger(eq("edits2"));
    }

    @Test
    public void testAreaTriggerSync() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        AreaTrigger areaTrigger = mock(AreaTrigger.class);

        when(areaTriggerManager.get(eq("SomeArea"))).thenReturn(areaTrigger);

        command.onCommand(sender, ITriggerCommand.toQueue("a sync SomeArea"));

        verify(areaTrigger).toggleSync();
    }

    @Test
    public void testCustomTriggerCreate() throws ClassNotFoundException {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());

        command.onCommand(sender, ITriggerCommand.toQueue("custom create onJoin onJoinDoSomething"));
        saveHandler.accept("MyScript edits");

        verify(customTriggerManager).createCustomTrigger(eq("onJoin"), eq("onJoinDoSomething"), eq("MyScript edits"));
    }

    @Test
    public void testCustomTriggerEdit() throws ClassNotFoundException,
            AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        CustomTrigger trigger = mock(CustomTrigger.class);

        when(customTriggerManager.get(anyString())).thenReturn(trigger);
        when(trigger.getScript()).thenReturn("");
        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());

        command.onCommand(sender, ITriggerCommand.toQueue("custom edit onJoinDoSomething"));
        saveHandler.accept("MyScript edits");

        verify(trigger).setScript(eq("MyScript edits"));
    }

    @Test
    public void testCustomTriggerDelete() throws ClassNotFoundException,
            AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        CustomTrigger trigger = mock(CustomTrigger.class);

        when(customTriggerManager.remove(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("custom delete onJoinDoSomething"));

        verify(sender).sendMessage(eq("&aRemoved the custom trigger &6onJoinDoSomething"));
    }

    @Test
    public void testCustomTriggerSync() throws ClassNotFoundException,
            AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        CustomTrigger trigger = mock(CustomTrigger.class);

        when(customTriggerManager.get(anyString())).thenReturn(trigger);
        when(trigger.getScript()).thenReturn("");
        when(trigger.isSync()).thenReturn(false);

        command.onCommand(sender, ITriggerCommand.toQueue("custom sync onJoinDoSomething"));

        verify(trigger).setSync(eq(true));
    }

    @Test
    public void testRepeatTriggerCreate() throws AbstractTriggerManager.TriggerInitFailedException, IOException {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());
        when(repeatingTriggerManager.createTrigger(anyString(), anyString())).thenReturn(true);

        command.onCommand(sender, ITriggerCommand.toQueue("r create myRepeat"));
        saveHandler.accept("some edits here");

        verify(repeatingTriggerManager).createTrigger(eq("myRepeat"), eq("some edits here"));
        verify(repeatingTriggerManager).showTriggerInfo(eq(sender), any());
    }

    @Test
    public void testRepeatTriggerEdit() throws AbstractTriggerManager.TriggerInitFailedException, IOException {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        RepeatingTrigger trigger = mock(RepeatingTrigger.class);

        when(trigger.getScript()).thenReturn("");
        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());
        when(repeatingTriggerManager.createTrigger(anyString(), anyString())).thenReturn(true);
        when(repeatingTriggerManager.get(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("r edit myRepeat"));
        saveHandler.accept("some edits here");

        verify(trigger).setScript(eq("some edits here"));
    }

    @Test
    public void testRepeatTriggerDelete() throws AbstractTriggerManager.TriggerInitFailedException, IOException {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        RepeatingTrigger trigger = mock(RepeatingTrigger.class);

        when(repeatingTriggerManager.get(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("r delete myRepeat"));

        verify(repeatingTriggerManager).remove(eq("myRepeat"));
    }

    @Test
    public void testRepeatTriggerInterval() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        RepeatingTrigger trigger = mock(RepeatingTrigger.class);

        when(repeatingTriggerManager.get(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("r interval myRepeat 30m20s"));

        verify(trigger).setInterval(eq(TimeUtil.parseTime("30m20s")));
    }

    @Test
    public void testRepeatTriggerAutostart() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        RepeatingTrigger trigger = mock(RepeatingTrigger.class);

        when(trigger.isAutoStart()).thenReturn(false);
        when(repeatingTriggerManager.get(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("r autostart myRepeat"));

        verify(trigger).setAutoStart(eq(true));
    }

    @Test
    public void testRepeatTriggerToggle() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        RepeatingTrigger trigger = mock(RepeatingTrigger.class);

        when(repeatingTriggerManager.get(anyString())).thenReturn(trigger);

        when(repeatingTriggerManager.isRunning(anyString())).thenReturn(false);
        command.onCommand(sender, ITriggerCommand.toQueue("r toggle myRepeat"));
        verify(repeatingTriggerManager).startTrigger(eq("myRepeat"));

        when(repeatingTriggerManager.isRunning(anyString())).thenReturn(true);
        command.onCommand(sender, ITriggerCommand.toQueue("r toggle myRepeat"));
        verify(repeatingTriggerManager).stopTrigger(eq("myRepeat"));
    }

    @Test
    public void testRepeatTriggerPause() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        RepeatingTrigger trigger = mock(RepeatingTrigger.class);

        when(repeatingTriggerManager.get(anyString())).thenReturn(trigger);

        when(trigger.isPaused()).thenReturn(false);
        command.onCommand(sender, ITriggerCommand.toQueue("r pause myRepeat"));
        verify(trigger).setPaused(eq(true));

        when(trigger.isPaused()).thenReturn(true);
        command.onCommand(sender, ITriggerCommand.toQueue("r pause myRepeat"));
        verify(trigger).setPaused(eq(false));
    }

    @Test
    public void testRepeatTriggerStatus() {
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        RepeatingTrigger trigger = mock(RepeatingTrigger.class);

        when(repeatingTriggerManager.get(anyString())).thenReturn(trigger);

        command.onCommand(sender, ITriggerCommand.toQueue("r status myRepeat"));

        verify(repeatingTriggerManager).showTriggerInfo(eq(sender), eq(trigger));
    }

    @Test
    public void testVariableLocation(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        SimpleLocation sloc = new SimpleLocation("world", 3, 6, -33);

        when(sender.getLocation()).thenReturn(sloc);

        command.onCommand(sender, ITriggerCommand.toQueue("vars loc test"));

        verify(variableManager).put(eq("test"), eq(sloc));
    }

    @Test
    public void testVariableItem(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        IItemStack itemStack = mock(IItemStack.class);
        Object instance = mock(Object.class);

        when(sender.getItemInMainHand()).thenReturn(itemStack);
        doReturn(instance).when(itemStack).get();

        command.onCommand(sender, ITriggerCommand.toQueue("vars item test"));

        verify(variableManager).put(eq("test"), eq(instance));
    }

    @Test
    public void testVariableItemNull(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        IItemStack itemStack = null;

        when(sender.getItemInMainHand()).thenReturn(itemStack);

        command.onCommand(sender, ITriggerCommand.toQueue("vars item test"));

        verify(variableManager, never()).put(eq("test"), any());
    }

    @Test
    public void testVariableLiteralBoolean(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        command.onCommand(sender, ITriggerCommand.toQueue("vars lit test true"));
        command.onCommand(sender, ITriggerCommand.toQueue("vars lit test2 false"));
        verify(variableManager).put(eq("test"), eq(true));
        verify(variableManager).put(eq("test2"), eq(false));
    }

    @Test
    public void testVariableLiteralNumber(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        command.onCommand(sender, ITriggerCommand.toQueue("vars lit test 55"));
        command.onCommand(sender, ITriggerCommand.toQueue("vars lit test2 885.223"));
        verify(variableManager).put(eq("test"), eq(55));
        verify(variableManager).put(eq("test2"), eq(885.223));
    }

    @Test
    public void testVariableLiteralOther(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        command.onCommand(sender, ITriggerCommand.toQueue("vars lit test 5555a"));
        command.onCommand(sender, ITriggerCommand.toQueue("vars lit test2 heyho"));
        verify(variableManager).put(eq("test"), eq("5555a"));
        verify(variableManager).put(eq("test2"), eq("heyho"));
    }

    @Test
    public void testVariableRead(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        when(variableManager.get(eq("test"))).thenReturn("this is test");

        command.onCommand(sender, ITriggerCommand.toQueue("vars read test"));

        verify(sender).sendMessage(eq("&7Value of test: this is test"));
    }

    @Test
    public void testVariableDelete(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        when(variableManager.has(eq("test"))).thenReturn(true);

        command.onCommand(sender, ITriggerCommand.toQueue("vars delete test"));

        verify(variableManager).remove(eq("test"));
    }

    @Test
    public void testItemTitle(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        IItemStack itemStack = mock(IItemStack.class);

        when(sender.getItemInMainHand()).thenReturn(itemStack);

        command.onCommand(sender, ITriggerCommand.toQueue("item title abcde"));

        verify(inventoryModifier).setItemTitle(eq(itemStack), eq("abcde"));
    }

    @Test
    public void testItemLoreAdd(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        IItemStack itemStack = mock(IItemStack.class);

        when(sender.getItemInMainHand()).thenReturn(itemStack);

        command.onCommand(sender, ITriggerCommand.toQueue("item lore add eeff"));

        verify(gameController).addItemLore(eq(itemStack), eq("eeff"));
        verify(sender).setItemInMainHand(eq(itemStack));
    }

    @Test
    public void testItemLoreSet(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        IItemStack itemStack = mock(IItemStack.class);

        when(sender.getItemInMainHand()).thenReturn(itemStack);
        when(inventoryModifier.setLore(any(), anyInt(), anyString())).thenReturn(true);

        command.onCommand(sender, ITriggerCommand.toQueue("item lore set 3 eeff"));

        verify(inventoryModifier).setLore(eq(itemStack), eq(2), eq("eeff"));
        verify(sender).setItemInMainHand(eq(itemStack));
    }

    @Test
    public void testItemLoreRemove(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);
        IItemStack itemStack = mock(IItemStack.class);

        when(sender.getItemInMainHand()).thenReturn(itemStack);
        when(inventoryModifier.removeLore(any(), anyInt())).thenReturn(true);

        command.onCommand(sender, ITriggerCommand.toQueue("item lore remove 5"));

        verify(inventoryModifier).removeLore(eq(itemStack), eq(4));
        verify(sender).setItemInMainHand(eq(itemStack));
    }

    @Test
    public void testTimingsToggle(){
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        boolean before = Timings.on;
        command.onCommand(sender, ITriggerCommand.toQueue("timings toggle"));

        assertEquals(!before, Timings.on);
    }

    @Test
    public void testDebug(){
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        command.onCommand(sender, ITriggerCommand.toQueue("debug"));

        verify(pluginLifecycleController).setDebugging(eq(true));
    }

    @Test
    public void testVersion(){
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        command.onCommand(sender, ITriggerCommand.toQueue("version"));

        verify(pluginLifecycleController).getVersion();
    }

    @Test
    public void testRun(){
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);
        CommandTrigger trigger = mock(CommandTrigger.class);
        Object event = mock(Object.class);

        when(commandTriggerManager.createTempCommandTrigger(anyString())).thenReturn(trigger);
        when(gameController.createEmptyPlayerEvent(eq(sender))).thenReturn(event);

        command.onCommand(sender, ITriggerCommand.toQueue("run #MESSAGE player.getName()"));

        verify(commandTriggerManager).createTempCommandTrigger(eq("#MESSAGE player.getName()"));
        verify(trigger).activate(anyMap());
    }

    @Test
    public void testSudo(){
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);
        IPlayer target = mock(IPlayer.class);
        CommandTrigger trigger = mock(CommandTrigger.class);
        Object event = mock(Object.class);

        when(gameController.getPlayer(eq("wysohn"))).thenReturn(target);
        when(commandTriggerManager.createTempCommandTrigger(anyString())).thenReturn(trigger);
        when(gameController.createEmptyPlayerEvent(eq(target))).thenReturn(event);

        command.onCommand(sender, ITriggerCommand.toQueue("sudo wysohn #MESSAGE player.getName()"));

        verify(gameController).createEmptyPlayerEvent(eq(target));
        verify(commandTriggerManager).createTempCommandTrigger(eq("#MESSAGE player.getName()"));
        verify(trigger).activate(anyMap());
    }

    @Test
    public void testCall(){
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);
        CommandTrigger trigger = mock(CommandTrigger.class);
        NamedTrigger targetTrigger = mock(NamedTrigger.class);
        Object event = mock(Object.class);

        when(commandTriggerManager.createTempCommandTrigger(anyString())).thenReturn(trigger);
        when(namedTriggerManager.get(eq("MyNamedTrigger"))).thenReturn(targetTrigger);
        when(gameController.createEmptyPlayerEvent(eq(sender))).thenReturn(event);

        command.onCommand(sender, ITriggerCommand.toQueue("call MyNamedTrigger abc = 4434;"));

        verify(commandTriggerManager).createTempCommandTrigger(eq("abc = 4434;"));
        verify(trigger).activate(anyMap());
        verify(targetTrigger).activate(anyMap());
    }

    @Test
    public void testList(){
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        Manager.ACTIVE_MANAGERS.add(commandTriggerManager);

        command.onCommand(sender, ITriggerCommand.toQueue("list CommandTrigger"));

        verify(commandTriggerManager).getTriggerList(any());
    }

    @Test
    public void testReload(){
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        Manager.ACTIVE_MANAGERS.add(commandTriggerManager);
        Manager.ACTIVE_MANAGERS.add(placeholderManager);
        Manager.ACTIVE_MANAGERS.add(executorManager);

        command.onCommand(sender, ITriggerCommand.toQueue("reload"));

        verify(commandTriggerManager).onReload();
        verify(executorManager).onReload();
        verify(placeholderManager).onReload();
    }

    @Test
    public void testLinkSave(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        command.onCommand(sender, ITriggerCommand.toQueue("links inveditsave"));

        verify(inventoryEditManager).onSaveEdit(eq(sender));
    }

    @Test
    public void testLinkContinue(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        command.onCommand(sender, ITriggerCommand.toQueue("links inveditcontinue"));

        verify(inventoryEditManager).onContinueEdit(eq(sender));
    }

    @Test
    public void testLinkDiscard(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        command.onCommand(sender, ITriggerCommand.toQueue("links inveditdiscard"));

        verify(inventoryEditManager).onDiscardEdit(eq(sender));
    }
}