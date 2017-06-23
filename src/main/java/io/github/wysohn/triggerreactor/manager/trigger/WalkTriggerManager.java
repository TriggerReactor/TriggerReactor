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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.TriggerManager;
import io.github.wysohn.triggerreactor.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.manager.location.SimpleLocation;

public class WalkTriggerManager extends LocationBasedTriggerManager<WalkTriggerManager.WalkTrigger> {
    public WalkTriggerManager(TriggerReactor plugin) {
        super(plugin, "WalkTrigger");
    }

    @Override
    protected WalkTrigger constructTrigger(String script) throws IOException, LexerException, ParserException {
        return new WalkTrigger(null, script);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerBlockLocationEvent e){
        handleWalk(e, e.getTo());
    }

    private boolean handleWalk(PlayerBlockLocationEvent e, SimpleLocation to){
        Player player = e.getPlayer();
        SimpleLocation bottomLoc = to.clone();
        bottomLoc.add(0, -1, 0);

        WalkTrigger trigger = getTriggerForLocation(bottomLoc);
        if(trigger == null)
            return false;

        Map<String, Object> varMap = new HashMap<>();
        insertPlayerVariables(player, varMap);
        varMap.put("from", e.getFrom());
        varMap.put("to", e.getTo());

        trigger.activate(e, varMap);
        return true;
    }

    class WalkTrigger extends TriggerManager.Trigger{

        public WalkTrigger(String name, String script) throws IOException, LexerException, ParserException {
            super(name, script);

            init();
        }

        @Override
        public Trigger clone() {
            try {
                return new WalkTrigger(triggerName, getScript());
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected String getTriggerTypeName() {
        return "Walk";
    }
}
