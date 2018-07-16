package io.github.wysohn.triggerreactor.sponge.manager;

import java.util.Map;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;

import io.github.wysohn.triggerreactor.core.manager.IScriptEngineInitializer;
import io.github.wysohn.triggerreactor.sponge.tools.TextUtil;

public interface SpongeScriptEngineInitializer extends IScriptEngineInitializer {

    @Override
    default void extractCustomVariables(Map<String, Object> variables, Object e) {
        // Thanks for the amazing API!
        if(e instanceof Event) {
            ((Event) e).getCause().first(Player.class).ifPresent((player)->{
                variables.put("player", player);
            });

            ((Event) e).getCause().first(Entity.class).ifPresent((entity)->{
                variables.put("entity", entity);
            });
        }
    }

    @Override
    default void initScriptEngine(ScriptEngineManager sem) throws ScriptException {
        IScriptEngineInitializer.super.initScriptEngine(sem);
        IScriptEngineInitializer.registerClass(sem, Sponge.class);
        IScriptEngineInitializer.registerClass(sem, TextUtil.class);
    }
}
