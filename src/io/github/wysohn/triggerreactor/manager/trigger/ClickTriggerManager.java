/*******************************************************************************
 *     Copyright (C) 2017 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
import io.github.wysohn.triggerreactor.core.wrapper.ObjectReference;
import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.TriggerManager;
import io.github.wysohn.triggerreactor.manager.location.SimpleLocation;

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
        varMap.put("player", new ObjectReference(player, "player"));
        varMap.put("block", new ObjectReference(clicked, "block"));

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
