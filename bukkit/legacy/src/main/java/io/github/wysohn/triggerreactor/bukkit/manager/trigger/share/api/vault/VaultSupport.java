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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.vault;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.APISupportException;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

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

        if (setupPermissions()) {
            plugin.getLogger().info("Vault permission hooked.");
        }
        if (setupChat()) {
            plugin.getLogger().info("Vault chat hooked.");
        }
        if (setupEconomy()) {
            plugin.getLogger().info("Vault economy hooked.");
        }
    }

    private boolean setupPermissions() {
        Plugin bukkitPlugin = plugin.getMain();
        RegisteredServiceProvider<Permission> permissionProvider = bukkitPlugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupChat() {
        Plugin bukkitPlugin = plugin.getMain();
        RegisteredServiceProvider<Chat> chatProvider = bukkitPlugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    private boolean setupEconomy() {
        Plugin bukkitPlugin = plugin.getMain();
        RegisteredServiceProvider<Economy> economyProvider = bukkitPlugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    /**
     * This allows the direct access to the object.
     *
     * @return
     */
    public Object permission() {
        if (permission == null)
            throw new APISupportException("Vault", "Permission");

        return permission;
    }

    /**
     * This allows the direct access to the object.
     *
     * @return
     */
    public Object economy() {
        if (economy == null)
            throw new APISupportException("Vault", "Economy");

        return economy;
    }

    /**
     * This allows the direct access to the object.
     *
     * @return
     */
    public Object chat() {
        if (chat == null)
            throw new APISupportException("Vault", "Chat");

        return chat;
    }

    /**
     * Check if player has the specified amount.
     *
     * @param offp
     * @param amount
     * @return true if has; false if not enough fund
     */
    public boolean has(Player offp, double amount) {
        if (economy == null)
            throw new APISupportException("Vault", "Economy");

        return economy.has(offp, amount);
    }

    /**
     * Give amount to the player. This return true most of the time, but it might be false if your economy
     * plugin has maximum limit, or any other reason to not allow adding more money to the player.
     *
     * @param player
     * @param amount
     * @return true on success; false on fail
     */
    public boolean give(Player player, double amount) {
        if (economy == null)
            throw new APISupportException("Vault", "Economy");

        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    /**
     * Take money from the player. It returns false if player doesn't have enough money.
     *
     * @param player
     * @param amount
     * @return true on success; false if not enough fund or any other reason depends on economy plugin.
     */
    public boolean take(Player player, double amount) {
        if (economy == null)
            throw new APISupportException("Vault", "Economy");

        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    /**
     * Set exact amount of money for specified player.
     *
     * @param player
     * @param amount
     * @return true most of time; false if something unexpected happen.
     */
    public boolean set(Player player, double amount) {
        if (economy == null)
            throw new APISupportException("Vault", "Economy");

        if (!economy.withdrawPlayer(player, balance(player)).transactionSuccess())
            throw new APISupportException("Vault", "Economy withdraw");

        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    /**
     * Get current balance of the player.
     *
     * @param player
     * @return balance of the player. It can be negative if the economy plugin allows it.
     */
    public double balance(Player player) {
        if (economy == null)
            throw new APISupportException("Vault", "Economy");

        return economy.getBalance(player);
    }

    /**
     * Add permission to specified player.
     *
     * @param player
     * @param perm
     */
    public void permit(Player player, String perm) {
        if (permission == null)
            throw new APISupportException("Vault", "Permission");

        permission.playerAdd(null, player, perm);
    }

    /**
     * Remove player from specified player.
     *
     * @param player
     * @param perm
     */
    public void revoke(Player player, String perm) {
        if (permission == null)
            throw new APISupportException("Vault", "Permission");

        permission.playerRemove(null, player, perm);
    }
}
