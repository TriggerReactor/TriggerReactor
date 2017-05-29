package io.github.wysohn.triggerreactor.manager.trigger.share.api.vault;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.trigger.share.api.APISupport;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class VaultSupport extends APISupport implements IVaultSupport {
    public Permission permission = null;
    public Economy economy = null;
    public Chat chat = null;

    public VaultSupport(TriggerReactor plugin) {
        super(plugin);

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

    @Override
    public boolean has(Player offp, Double amount) {
        if(economy == null)
            throw new VaultSupportException("Economy");

        return economy.has(offp, amount);
    }

    @Override
    public boolean give(Player player, Double amount) {
        if(economy == null)
            throw new VaultSupportException("Economy");

        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    @Override
    public boolean take(Player player, Double amount) {
        if(economy == null)
            throw new VaultSupportException("Economy");

        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    @Override
    public boolean set(Player player, Double amount) {
        if(economy == null)
            throw new VaultSupportException("Economy");

        if(!economy.withdrawPlayer(player, balance(player)).transactionSuccess())
            throw new VaultSupportException("Economy withdraw");

        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    @Override
    public double balance(Player player) {
        if(economy == null)
            throw new VaultSupportException("Economy");

        return economy.getBalance(player);
    }

}
