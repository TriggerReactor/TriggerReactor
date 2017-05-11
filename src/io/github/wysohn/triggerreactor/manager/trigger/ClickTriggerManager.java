package io.github.wysohn.triggerreactor.manager.trigger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.TriggerManager;
import io.github.wysohn.triggerreactor.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.manager.wrapper.PlayerWrapper;
import io.github.wysohn.triggerreactor.manager.wrapper.Wrapper;

public class ClickTriggerManager extends LocationBasedTriggerManager<ClickTriggerManager.ClickTrigger> {
    public ClickTriggerManager(TriggerReactor plugin) {
        super(plugin, "ClickTrigger");
    }

    @Override
    protected ClickTrigger constructTrigger(String script) throws IOException, LexerException, ParserException {
        return new ClickTrigger(script, new ClickHandler(){
            @Override
            public boolean allow(Action action) {
                return action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK;
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onClickTrigger(PlayerInteractEvent e){
        if(handleClick(e)){
            e.setCancelled(true);
        }
    }

    private boolean handleClick(PlayerInteractEvent e){
        Player player = e.getPlayer();
        Block clicked = e.getClickedBlock();

        Location loc = clicked.getLocation();
        ClickTrigger trigger = getTriggerForLocation(loc);
        if(trigger == null)
            return false;

        Map<String, Object> varMap = new HashMap<>();
        varMap.putAll(Wrapper.wrapperToVariablesMap(new PlayerWrapper(player)));

        trigger.activate(e, varMap);
        return true;
    }

    @Override
    protected void onLocationChange(PlayerMoveEvent e, SimpleLocation from, SimpleLocation to) {
        // TODO Auto-generated method stub

    }

    class ClickTrigger extends TriggerManager.Trigger{
        private ClickHandler handler;

        public ClickTrigger(String script, ClickHandler handler) throws IOException, LexerException, ParserException {
            super(script);
            this.handler = handler;
        }

        @Override
        public void activate(Event e, Map<String, Object> scriptVars) {
            Action action = ((PlayerInteractEvent) e).getAction();
            if(!handler.allow(action))
                return;

            super.activate(e, scriptVars);
        }
    }

    public interface ClickHandler{
        boolean allow(Action action);
    }
}
