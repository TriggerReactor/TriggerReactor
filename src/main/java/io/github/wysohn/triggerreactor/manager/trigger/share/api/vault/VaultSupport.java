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
package io.github.wysohn.triggerreactor.manager.trigger.share.api.vault;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.manager.trigger.share.api.APISupportException;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class VaultSupport extends APISupport {
    public Permission permission = null;
    public Economy economy = null;
    public Chat chat = null;

    public VaultSupport(TriggerReactor plugin) {
        super(plugin, "Vault");
    }

    @Override
    public void init() throws APISupportException {
        super.init();

        if(setupPermissions()){
            plugin.getLogger().info("Vault permission hooked.");
        }
        if(setupChat()){
            plugin.getLogger().info("Vault chat hooked.");
        }
        if(setupEconomy()){
            plugin.getLogger().info("Vault economy hooked.");
        }
    }

    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupChat()
    {
        RegisteredServiceProvider<Chat> chatProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    public Object permission() {
        if(permission == null)
            throw new APISupportException("Vault", "Permission");

        return permission;
    }

    public Object economy() {
        if(economy == null)
            throw new APISupportException("Vault", "Economy");

        return economy;
    }

    public Object chat() {
        if(chat == null)
            throw new APISupportException("Vault", "Chat");

        return chat;
    }

    public boolean has(Player offp, Double amount) {
        if(economy == null)
            throw new APISupportException("Vault", "Economy");

        return economy.has(offp, amount);
    }

    public boolean give(Player player, Double amount) {
        if(economy == null)
            throw new APISupportException("Vault", "Economy");

        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public boolean take(Player player, Double amount) {
        if(economy == null)
            throw new APISupportException("Vault", "Economy");

        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean set(Player player, Double amount) {
        if(economy == null)
            throw new APISupportException("Vault", "Economy");

        if(!economy.withdrawPlayer(player, balance(player)).transactionSuccess())
            throw new APISupportException("Vault", "Economy withdraw");

        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public double balance(Player player) {
        if(economy == null)
            throw new APISupportException("Vault", "Economy");

        return economy.getBalance(player);
    }
}
