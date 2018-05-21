package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractSignTriggerManager;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public class SignTriggerManager extends AbstractSignTriggerManager implements BukkitTriggerManager {

    public SignTriggerManager(TriggerReactor plugin) {
        super(plugin, new CommonFunctions(plugin), new File(plugin.getDataFolder(), "SignTrigger"));
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if(e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if(!BukkitUtil.isLeftHandClick(e))
            return;

        if(!isSign(e.getClickedBlock().getType()))
            return;

        Sign sign = (Sign) e.getClickedBlock().getState();
        String[] lines = sign.getLines();

        SignTrigger trigger = this.getSignTriggerByLines(lines);
        if(trigger == null)
            return;

        Map<String, Object> varMap = new HashMap<String, Object>();
        varMap.put("player", e.getPlayer());
        varMap.put("block", e.getClickedBlock());
        varMap.put("item", e.getItem());

        trigger.activate(e, varMap);
    }

    private boolean isSign(Material type) {
        return type == Material.SIGN_POST || type == Material.WALL_SIGN;
    }

    @Override
    protected void deleteInfo(Trigger trigger) {
        File yamlFile = new File(folder, trigger.getTriggerName()+".yml");
        FileUtil.delete(yamlFile);
        super.deleteInfo(trigger);
    }


}
