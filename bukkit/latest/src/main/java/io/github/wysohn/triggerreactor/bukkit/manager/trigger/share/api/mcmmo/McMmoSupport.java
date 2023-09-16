/*
 * Copyright (C) 2023. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.mcmmo;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SuperAbilityType;
import com.gmail.nossr50.util.player.UserManager;
import com.google.inject.Injector;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import org.bukkit.entity.Player;

public class McMmoSupport extends APISupport {
    public McMmoSupport(Injector injector) {
        super(injector, "mcMMO");
    }

    /**
     * get MCMMOPlayer object directly.
     *
     * @param name
     * @return
     */
    public Object player(String name) {
        return UserManager.getOfflinePlayer(name);
    }

    /**
     * get current level of player's 'skillType'
     *
     * @param player
     * @param skillType
     * @return
     */
    public int level(Player player, String skillType) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if (mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player " + player.getName());

        PrimarySkillType type = PrimarySkillType.getSkill(skillType);

        return mmoPlayer.getSkillLevel(type);
    }

    /**
     * set level of target player's 'skillType' to 'level'
     *
     * @param player
     * @param skillType
     * @param level
     */
    public void setLevel(Player player, String skillType, int level) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if (mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player " + player.getName());

        PrimarySkillType type = PrimarySkillType.getSkill(skillType);

        mmoPlayer.modifySkill(type, level);
    }

    /**
     * add 'levels' to target player's 'skillType'
     *
     * @param player
     * @param skillType
     * @param levels
     */
    public void addLevel(Player player, String skillType, int levels) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if (mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player " + player.getName());

        PrimarySkillType type = PrimarySkillType.getSkill(skillType);

        mmoPlayer.addLevels(type, levels);
    }

    /**
     * get current xp of player's 'skillType'
     *
     * @param player
     * @param skillType
     * @return
     */
    public double xp(Player player, String skillType) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if (mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player " + player.getName());

        PrimarySkillType type = PrimarySkillType.getSkill(skillType);

        return mmoPlayer.getSkillXpLevelRaw(type);
    }

    /**
     * add 'xp' to the target player's 'skillType'
     *
     * @param player
     * @param skillType
     * @param xp
     */
    public void addXp(Player player, String skillType, int xp) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if (mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player " + player.getName());

        PrimarySkillType type = PrimarySkillType.getSkill(skillType);

        mmoPlayer.addXp(type, xp);
    }

    /**
     * remove 'xp' from target player's 'skillType'
     *
     * @param player
     * @param skillType
     * @param xp
     */
    public void removeXp(Player player, String skillType, int xp) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if (mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player " + player.getName());

        PrimarySkillType type = PrimarySkillType.getSkill(skillType);

        mmoPlayer.removeXp(type, xp);
    }

    /**
     * get xp left until level up of 'skillType' for player.
     *
     * @param player
     * @param skillType
     * @return
     */
    public double xpRemain(Player player, String skillType) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if (mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player " + player.getName());

        PrimarySkillType type = PrimarySkillType.getSkill(skillType);

        return mmoPlayer.getXpToLevel(type);
    }

    /**
     * get cooltime of player's 'abilityType'
     *
     * @param player
     * @param abilityType
     * @return
     */
    public int cooldown(Player player, String abilityType) {
        McMMOPlayer mmoPlayer = (McMMOPlayer) player(player.getName());
        if (mmoPlayer == null)
            throw new RuntimeException("Could not find MCMMO info for player " + player.getName());

        SuperAbilityType type = SuperAbilityType.valueOf(abilityType);

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
