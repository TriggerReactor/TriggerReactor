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
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.selection.AreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.selection.LocationSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.click.ClickTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.walk.WalkTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTriggerManager;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TriggerCommandTest {
    IPluginLifecycleController pluginLifecycleController = mock(IPluginLifecycleController.class);
    IGameController gameController = mock(IGameController.class);
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
        builder = DaggerTriggerCommandTestComponent.builder()
                .pluginLifecycle(pluginLifecycleController)
                .gameController(gameController)
                .throwableHandler(throwableHandler)
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
    public void printHierarchy(){
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
        SimpleLocation clickedLoc = new SimpleLocation("World", 4 , 55, 22);

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
        SimpleLocation clickedLoc = new SimpleLocation("World", 4 , 55, 22);
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
        SimpleLocation clickedLoc = new SimpleLocation("World", 8 , 12, -88);

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
        SimpleLocation clickedLoc = new SimpleLocation("World", 8 , 12, -88);
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
    public void testSearchPlayerOnly(){
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        command.onCommand(sender, ITriggerCommand.toQueue("search"));

        verify(gameController, never()).showGlowStones(eq(sender), anySet());
    }

    @Test
    public void testSearch(){
        ITriggerCommand command = builder.build().triggerCommand();
        IPlayer sender = mock(IPlayer.class);

        command.onCommand(sender, ITriggerCommand.toQueue("search"));

        verify(clickTriggerManager).getTriggersInChunk(any());
        verify(walkTriggerManager).getTriggersInChunk(any());
        verify(gameController, times(2)).showGlowStones(eq(sender), anySet());
    }

    @Test
    public void testCommandTriggerNew(){
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());

        command.onCommand(sender, ITriggerCommand.toQueue("cmd new MyCommand #MESSAGE \"Hi\""));
        saveHandler.accept("#MESSAGE \"Hi\"");

        verify(commandTriggerManager).addCommandTrigger(eq("MyCommand"), eq("#MESSAGE \"Hi\""));
        verify(editManager).startEdit(any(), anyString(), eq("#MESSAGE \"Hi\""), eq(saveHandler), eq(true));
    }

    @Test
    public void testCommandTriggerNewAlreadyExist(){
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);

        when(commandTriggerManager.has(anyString())).thenReturn(true);
        doAnswer(invocation -> {
            assertEquals(sender, invocation.getArgument(0));
            saveHandler = invocation.getArgument(3);
            return null;
        }).when(editManager).startEdit(any(), anyString(), anyString(), any(), anyBoolean());

        command.onCommand(sender, ITriggerCommand.toQueue("cmd new MyCommand #MESSAGE \"Hi\""));

        verify(commandTriggerManager, never()).addCommandTrigger(eq("MyCommand"), eq("#MESSAGE \"Hi\""));
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
        doThrow(new AbstractTriggerManager.TriggerInitFailedException("", null))
                .when(trigger).setScript(anyString());

        command.onCommand(sender, ITriggerCommand.toQueue("cmd edit MyCommand"));
        saveHandler.accept("#MESSAGE \"Hello\"");

        verify(trigger).setScript(eq("#MESSAGE \"Hello\""));
        verify(throwableHandler).handleException(eq(sender), any(AbstractTriggerManager.TriggerInitFailedException.class));
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
        TriggerInfo info = mock(TriggerInfo.class);

        when(commandTriggerManager.has(anyString())).thenReturn(true);
        when(commandTriggerManager.get(anyString())).thenReturn(trigger);
        when(trigger.getInfo()).thenReturn(info);

        command.onCommand(sender, ITriggerCommand.toQueue("cmd sync MyCommand"));

        verify(info).setSync(true);
    }

    @Test
    public void testCommandTriggerPermission() throws AbstractTriggerManager.TriggerInitFailedException {
        ITriggerCommand command = builder.build().triggerCommand();
        ICommandSender sender = mock(ICommandSender.class);
        CommandTrigger trigger = mock(CommandTrigger.class);
        TriggerInfo info = mock(TriggerInfo.class);

        when(commandTriggerManager.has(anyString())).thenReturn(true);
        when(commandTriggerManager.get(anyString())).thenReturn(trigger);
        when(trigger.getInfo()).thenReturn(info);

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
        when(trigger.getInfo()).thenReturn(info);

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
        when(trigger.getInfo()).thenReturn(info);

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
        when(trigger.getInfo()).thenReturn(info);

        command.onCommand(sender, ITriggerCommand.toQueue("cmd a MyCommand"));

        verify(trigger).setAliases(eq(null));
        verify(commandTriggerManager).reregisterCommand(eq("MyCommand"));
    }

    @Test
    public void testCommandTriggerTabs() {
        //TODO tab completion will be reworked soon
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
        when(trigger.rows()).thenReturn(6);

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
}