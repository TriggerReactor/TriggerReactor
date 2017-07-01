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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;

public class ClickTriggerManager extends LocationBasedTriggerManager<AbstractLocationBasedTriggerManager.ClickTrigger> {
    public ClickTriggerManager(TriggerReactor plugin) {
        super(plugin, "ClickTrigger");
    }

    @Override
    protected ClickTrigger constructTrigger(String script) throws IOException, LexerException, ParserException {
        return new ClickTrigger(null, script, new ClickHandler(){
            @Override
            public boolean allow(Action action) {
                return action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK;
            }
        });
    }

    @EventHandler()
    public void onClickTrigger(PlayerInteractEvent e){
        if(e.getHand() != EquipmentSlot.HAND)
            return;

        if(!e.isCancelled() && handleClick(e)){
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
        varMap.put("player", e.getPlayer());
        varMap.put("block", clicked);
        varMap.put("item", e.getItem());

        trigger.activate(e, varMap);
        return true;
    }

    @Override
    protected String getTriggerTypeName() {
        return "Click";
    }
}
