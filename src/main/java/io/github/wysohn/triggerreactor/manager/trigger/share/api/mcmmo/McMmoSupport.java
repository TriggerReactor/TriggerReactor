package io.github.wysohn.triggerreactor.manager.trigger.share.api.mcmmo;

import org.bukkit.entity.Player;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.AbilityType;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;

import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.trigger.share.api.APISupport;

public class McMmoSupport extends APISupport {

    public McMmoSupport(TriggerReactor plugin) {
        super(plugin, "mcMMO");
    }

    public Object player(String name) {
        return UserManager.getOfflinePlayer(name);
    }

    public int level(Player player, String skillType) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if(mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player "+player.getName());

        SkillType type = SkillType.valueOf(skillType);

        return mmoPlayer.getSkillLevel(type);
    }

    public void setLevel(Player player, String skillType, int level) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if(mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player "+player.getName());

        SkillType type = SkillType.valueOf(skillType);

        mmoPlayer.modifySkill(type, level);
    }

    public void addLevel(Player player, String skillType, int levels) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if(mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player "+player.getName());

        SkillType type = SkillType.valueOf(skillType);

        mmoPlayer.addLevels(type, levels);
    }

    public double xp(Player player, String skillType) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if(mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player "+player.getName());

        SkillType type = SkillType.valueOf(skillType);

        return mmoPlayer.getSkillXpLevelRaw(type);
    }

    public void addXp(Player player, String skillType, int xp) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if(mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player "+player.getName());

        SkillType type = SkillType.valueOf(skillType);

        mmoPlayer.addXp(type, xp);
    }

    public void removeXp(Player player, String skillType, int xp) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if(mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player "+player.getName());

        SkillType type = SkillType.valueOf(skillType);

        mmoPlayer.removeXp(type, xp);
    }

    public double xpRemain(Player player, String skillType) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if(mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player "+player.getName());

        SkillType type = SkillType.valueOf(skillType);

        return mmoPlayer.getXpToLevel(type);
    }

    public int cooldown(Player player, String abilityType) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if(mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player "+player.getName());

        AbilityType type = AbilityType.valueOf(abilityType);

        return mmoPlayer.calculateTimeRemaining(type);
    }

/*    public void setCooldown(Player player, String abilityType, int cd) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if(mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player "+player.getName());

        AbilityType type = AbilityType.valueOf(abilityType);

        return mmoPlayer.
    }

    public void resetCooldown(Player player) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if(mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player "+player.getName());

        AbilityType type = AbilityType.valueOf(abilityType);

        return mmoPlayer.calculateTimeRemaining(type);
    }*/

}
