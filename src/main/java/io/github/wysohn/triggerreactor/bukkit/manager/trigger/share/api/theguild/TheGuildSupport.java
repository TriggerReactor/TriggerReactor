package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.theguild;

import java.util.Set;

import org.bukkit.entity.Player;
import org.theguild.costants.guild.Guild;
import org.theguild.costants.member.Member;
import org.theguild.main.TheGuild;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupportException;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;

public class TheGuildSupport extends APISupport {

    public TheGuildSupport(TriggerReactor plugin) {
        super(plugin, "TheGuild");
    }

    @Override
    public void init() throws APISupportException {
        super.init();
    }

    public Member member(Player player){
        return TheGuild.getMemberManager().getSession(player);
    }

    public Guild guild(String name){
        return TheGuild.getGuildManager().getGuild(name);
    }

    public Set<Guild> allGuild(){
        return TheGuild.getGuildManager().getAllGuilds();
    }

    public void guildGiveExp(Guild guild, double exp){
        TheGuild.getGuildManager().giveExp(guild, exp);
    }

    public boolean guildGiveMoney(Guild guild, double money){
        return guild.deposit(null, money);
    }

    public boolean guildTakeMoney(Guild guild, double money){
        return guild.withdraw(null, money);
    }

    public void guildSetMoney(Guild guild, double money){
        guild.setBalance(money);
    }

    public boolean guildHasMoney(Guild guild, double money){
        return guild.has(money);
    }
}
