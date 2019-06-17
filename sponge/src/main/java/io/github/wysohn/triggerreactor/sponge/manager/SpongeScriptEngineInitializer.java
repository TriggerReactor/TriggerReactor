package io.github.wysohn.triggerreactor.sponge.manager;

import com.flowpowered.math.vector.Vector3d;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineInitializer;
import io.github.wysohn.triggerreactor.sponge.tools.TextUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public interface SpongeScriptEngineInitializer extends IScriptEngineInitializer {
    @Override
    default void initScriptEngine(ScriptEngineManager sem) throws ScriptException {
        IScriptEngineInitializer.super.initScriptEngine(sem);
        IScriptEngineInitializer.registerClass(sem, Sponge.class);
        IScriptEngineInitializer.registerClass(sem, TextUtil.class);
        IScriptEngineInitializer.registerClass(sem, Location.class);
        IScriptEngineInitializer.registerClass(sem, ItemStack.class);
        IScriptEngineInitializer.registerClass(sem, Text.class);
        IScriptEngineInitializer.registerClass(sem, TextColors.class);
        IScriptEngineInitializer.registerClass(sem, ChatTypes.class);
        IScriptEngineInitializer.registerClass(sem, Keys.class);
        IScriptEngineInitializer.registerClass(sem, PotionEffect.class);
        IScriptEngineInitializer.registerClass(sem, PotionEffectTypes.class);
        IScriptEngineInitializer.registerClass(sem, EntityTypes.class);
        IScriptEngineInitializer.registerClass(sem, ItemTypes.class);
        IScriptEngineInitializer.registerClass(sem, Enchantment.class);
        IScriptEngineInitializer.registerClass(sem, EnchantmentTypes.class);
        IScriptEngineInitializer.registerClass(sem, BlockTypes.class);
        IScriptEngineInitializer.registerClass(sem, Cause.class);
        IScriptEngineInitializer.registerClass(sem, EventContext.class);
        IScriptEngineInitializer.registerClass(sem, SoundType.class);
        IScriptEngineInitializer.registerClass(sem, SoundTypes.class);
        IScriptEngineInitializer.registerClass(sem, Vector3d.class);
    }
}
