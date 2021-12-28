/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
package io.github.wysohn.triggerreactor.sponge.manager.trigger;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.tools.FileUtil;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClickTriggerManager extends LocationBasedTriggerManager<AbstractLocationBasedTriggerManager.ClickTrigger> {
    public ClickTriggerManager(TriggerReactorCore plugin) {
        super(plugin, "ClickTrigger", new ITriggerLoader<ClickTrigger>() {
            @Override
            public ClickTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
                try {
                    String script = FileUtil.readFromFile(info.getSourceCodeFile());
                    ClickTrigger trigger = getTrigger(info, script);
                    return trigger;
                } catch (TriggerInitFailedException | IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void save(ClickTrigger trigger) {
                try {
                    FileUtil.writeToFile(trigger.getInfo().getSourceCodeFile(), trigger.getScript());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static ClickTrigger getTrigger(TriggerInfo info, String script) throws TriggerInitFailedException {
        return new ClickTrigger(info, script, context -> {
            if (context instanceof InteractBlockEvent) {
                return context instanceof InteractBlockEvent.Primary.MainHand
                        || context instanceof InteractBlockEvent.Secondary.MainHand;
            }

            return true;
        });
    }

    @Override
    protected String getTriggerTypeName() {
        return "Click";
    }

    @Override
    protected ClickTrigger newTrigger(TriggerInfo info, String script) throws TriggerInitFailedException {
        return getTrigger(info, script);
    }

    @Listener
    @Exclude({InteractBlockEvent.Primary.OffHand.class, InteractBlockEvent.Secondary.OffHand.class})
    public void onClickTrigger(InteractBlockEvent e) {
        handleClick(e);
    }

    private void handleClick(InteractBlockEvent e) {
        Player player = e.getCause().first(Player.class).orElse(null);
        BlockSnapshot clicked = e.getTargetBlock();

        Location<World> loc = clicked.getLocation().orElse(null);
        if (loc == null)
            return;

        ClickTrigger trigger = getTriggerForLocation(loc);
        if (trigger == null)
            return;

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", player);
        varMap.put("block", clicked);
        varMap.put("item", player.getItemInHand(HandTypes.MAIN_HAND).orElse(ItemStack.of(ItemTypes.AIR, 1)));
        if (e instanceof InteractBlockEvent.Primary.MainHand)
            varMap.put("click", "left");
        else if (e instanceof InteractBlockEvent.Secondary.MainHand)
            varMap.put("click", "right");
        else
            varMap.put("click", "unknown");

        trigger.activate(e, varMap);
        return;
    }
}
