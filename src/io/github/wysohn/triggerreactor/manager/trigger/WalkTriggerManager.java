package io.github.wysohn.triggerreactor.manager.trigger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.TriggerManager;
import io.github.wysohn.triggerreactor.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.manager.wrapper.PlayerWrapper;
import io.github.wysohn.triggerreactor.manager.wrapper.Wrapper;

public class WalkTriggerManager extends LocationBasedTriggerManager<WalkTriggerManager.WalkTrigger> {
    public WalkTriggerManager(TriggerReactor plugin) {
        super(plugin, "WalkTrigger");
    }

    @Override
    protected WalkTrigger constructTrigger(String script) throws IOException, LexerException, ParserException {
        return new WalkTrigger(script);
    }

    @Override
    protected void onLocationChange(PlayerMoveEvent e, SimpleLocation from, SimpleLocation to) {
        handleWalk(e, to);
    }

    private boolean handleWalk(PlayerMoveEvent e, SimpleLocation to){
        Player player = e.getPlayer();
        SimpleLocation bottomLoc = to.clone();
        bottomLoc.add(0, -1, 0);

        WalkTrigger trigger = getTriggerForLocation(bottomLoc);
        if(trigger == null)
            return false;

        Map<String, Object> varMap = new HashMap<>();
        varMap.putAll(Wrapper.wrapperToVariablesMap(new PlayerWrapper(player)));

        trigger.activate(e, varMap);
        return true;
    }

    class WalkTrigger extends TriggerManager.Trigger{

        public WalkTrigger(String script) throws IOException, LexerException, ParserException {
            super(script);

        }

        @Override
        public void activate(Event e, Map<String, Object> scriptVars) {
            super.activate(e, scriptVars);
        }
    }
}
