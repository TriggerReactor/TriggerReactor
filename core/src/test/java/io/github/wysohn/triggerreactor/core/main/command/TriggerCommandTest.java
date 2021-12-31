package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.components.DaggerTriggerCommandTestComponent;
import io.github.wysohn.triggerreactor.components.TriggerCommandTestComponent;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.selection.AreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.selection.LocationSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTriggerManager;
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
}