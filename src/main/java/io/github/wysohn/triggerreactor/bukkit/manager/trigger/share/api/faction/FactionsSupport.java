package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.faction;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MConfColl;
import com.massivecraft.factions.entity.MFlagColl;
import com.massivecraft.factions.entity.MPermColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.entity.MPlayerColl;
import com.massivecraft.massivecore.ps.PS;
import com.massivecraft.massivecore.util.IdUtil;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupportException;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;

public class FactionsSupport extends APISupport {
    /**
     * Represents empty zone
     */
    protected Faction none;
    /**
     * Represents safe zone
     */
    protected Faction safeZone;
    /**
     * Represents ware zone
     */
    protected Faction warZone;

    public FactionsSupport(TriggerReactor plugin) {
        super(plugin, "Factions");
    }

    @Override
    public void init() throws APISupportException {
        super.init();

        none = FactionColl.get().getNone();
        safeZone = FactionColl.get().getSafezone();
        warZone = FactionColl.get().getWarzone();
    }

    /**
     * Direct object access. Use if you have specific knowledge about the FactionsAPI
     * @return
     */
    public Object boardColl(){
        return BoardColl.get();
    }

    /**
     * Direct object access. Use if you have specific knowledge about the FactionsAPI
     * @return
     */
    public Object factionColl(){
        return FactionColl.get();
    }

    /**
     * Direct object access. Use if you have specific knowledge about the FactionsAPI
     * @return
     */
    public Object mConfColl(){
        return MConfColl.get();
    }

    /**
     * Direct object access. Use if you have specific knowledge about the FactionsAPI
     * @return
     */
    public Object mFlagColl(){
        return MFlagColl.get();
    }

    /**
     * Direct object access. Use if you have specific knowledge about the FactionsAPI
     * @return
     */
    public Object mPermColl(){
        return MPermColl.get();
    }

    /**
     * Direct object access. Use if you have specific knowledge about the FactionsAPI
     * @return
     */
    public Object mPlayerColl(){
        return MPlayerColl.get();
    }

    /**
     * get MPlayer specified by the Player
     * @param player
     * @return
     */
    public MPlayer player(Player player){
        return MPlayer.get(player);
    }

    /**
     * get MPlayer sepcified by the UUID
     * @param uuid
     * @return
     */
    public MPlayer player(UUID uuid){
        return MPlayer.get(uuid);
    }

    /**
     * get MPlayer from player name
     * @param name
     * @return
     */
    public MPlayer player(String name){
        return MPlayer.get(IdUtil.getId(name));
    }

    /**
     * get Faction at the specified Location.
     * @param loc
     * @return
     */
    public Faction faction(Location loc){
        return BoardColl.get().getFactionAt(PS.valueOf(loc));
    }

    /**
     * get Faction by name
     * @param name
     * @return
     */
    public Faction faction(String name){
        return FactionColl.get().getByName(name);
    }
}
