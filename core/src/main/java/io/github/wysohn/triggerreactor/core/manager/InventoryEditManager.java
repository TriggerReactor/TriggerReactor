/*
 * Copyright (C) 2022. TriggerReactor Team
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

package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IInventoryHandle;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * <a href="https://online.visual-paradigm.com/w/rbqxgvrk/diagrams/?lightbox=1&highlight=0000ff&edit=https%3A%2F%2Fonline.visual-paradigm.com%2Fw%2Frbqxgvrk%2Fdiagrams%2F%23diagram%3Aworkspace%3Drbqxgvrk%26proj%3D20%26id%3D48&editBlankUrl=https%3A%2F%2Fonline.visual-paradigm.com%2Fapp%2Fdiagrams%2F%23diagram%3Aproj%3D0%26vpov%3D16.3%26vpob%3D20220410%26client%3D1%26edit%3D_blank&layers=1&nav=1&title=InventoryEditManager&vpov=16.3&vpob=20220410#R3cU2FsdUGVkX1S%2FvempGZFrs2hHJMT4DHwLCE7ddB%2BqMnRucA8BZ8%3Du8qqTWP5Vc4j1Bw77YV2WiprLUtIkp2fr5H2KDgf55oDx7T9JzNFXJLHWHFn2MgOHDbzOLrCyUZRwOTCjnDVoRLmAjeI%2B36rFGZFT4lvUUswgk2l5hNLru7EYJX0fnIp7p%2FgoP8cyapc6bHOu%2F9XFasiinYxd6ZdCUeLzyk5qwaS5a6M8qz7pUkBFMg4z4VDr6f8UrtB%2BqGC1IBfRFUdGDvnQtnjQhQMWQ%2FYgkiav6D9esmzbd3X3rlyhXu4ZA0f63FeCzF3vfZw2mjOJsVytTjGuIvoo8rcZ9oJWhoeyejbTiANDwqSYk2Xq0YG4qsvFuiLfua5bfP84z3Y4M6%2FU8gwOxvS3e1b9TeXtQjl1CRuzHUCbuloTNUbh3loeO5YHOa57oCr49bKSNuOCH2rMLSNkxdGb1aLgB603Y3x2tNGMmme3C0JRxW8pafDcnCQ%2FGZ2gQdwFtwnaOOhJ26Cc%2BPasvnv23z075xaj43jUfv%2Fz2NZBCwHDzOr4eVR79SCdonIPc1rjgpgVJTVNcZ4dP%2F2qGjXOrzgGxs96ZLnmefpKLib5Kubab0G7RvDHjv%2F%2FOkiUkCkurnwUVHcPe1fKeoutVMDpmyQ3hFVJGE7RJ%2F3Oz1nr3RwI0sXD7KZg%2BnZJYaLlL9kIyS0dbzjH7YuiBDdRI9dQPs2bKvSBP9UYqWUB9j3Bqqx5imvtTkHdv4cVXK3qXCSaLN%2F8jXzNOISozX3z6tWeLNYLG3n9vB80hCqWpyqtBUaDjMkq5QuS2F6UJtk9DZJft9tmfiiXYwwS5Vk9Q0mwe%2Fb7iK%2FavviJNNdQH42zRvM9%2ByxhTAKIsH780Uti9RzIHGecIDkYS2QLgIJ7%2BEhzpg9K%2Bpf1kS4OTyohruX5L%2Fbs3NhBU25Oz4Jb%2BsWmlyJEfO5Bv4WKfOyzV1GVsiSGDA1hp8y0nvY2aI6KYDVoMQU3e5m9i3ozBd2AC1lQEkGq%2B%2BYIrSJQXDSx6VlNyBuGbGA8LtAjwOXlQ8eTMZ5lMrX7SlE5bRvmIComKd3oSTCJAlz430Kd58mt2JZa7MxasdCbCYefPS25JS%2FFYEjgnW1AQRzuNunkS8X5OpR2hXubLez75b9w%2BWNZHFBchgABISBblESydrOeIvbQbHwfjTSks5f8%2Bjn8WquaQvDedfibiV%2Bu%2F%2FAbvMBF97QK6ll3UDZs5HfYmvpqJXhHwuPOr4AqQxBt64%2Fw83ytpBhPJKyVPXqHDcMu%2FAfWNiE9S8Abidqd2JtTiWFqWB7E5XoYRLaDFZfoW5lSS%2F7yAwulhUc1dttORgbHk%2FsKjxI3zNJmvh4vAlXyywrDShJlCw4ko58pWG%2Fzd2IMFUbQ49TaE4YSkMY82UmgNOQ2R%2BPMBLUafu5gjKyhU%2FSCZJPjAn22ORmZboY2LUmsAndSK75x6wAFhKm6xrUVqfsVfL72I8VGKa8nMPSisS2u9DW2Tth%2FEgeAIdrhCjAe2y6HNIhz2Dde5wEm%2FloAs1%2BrHt6lNZDYKJHE3PYXQy8ILg%2B5yDiS%2BLA9LKdb1Gzi72dpY7r2IEuyb9WhxyPxAJrSG4R2GTKD%2BCRTF1d1gJ4BqCgY89Y0DjqiYvE37gQnf8yw%2FTd0H4vInWEdBkFbX1uhl%2B7PBrlqPOPVbdPKCh9AYA5SQP90wVlyISBm7qpw%2FGmCZpMTwrJuB3Tzqqe830X11%2BcefMjAN7Zf1JnHzDB2wKWwEMoaN2KLlyNyjj7D2bT9aOQCyj06Hy4l6gYFEzkn6kq0HhZI2WUneaQ5W3%2BsXDfT01FPANW%2BMCSRMUE5k9MF30zI7MPtgOKkCF7kBvTYJknUuMc4BzcOSoyW8t48bwRM00ZSuIRySndBf63iuGrrumKbXOG5LtHFw7fMsrVeaMFzvyWHbm5HLhys7FhtZzMiL79NBn%2FoiWz9DKgDPpYkU6faSbf2Z7oKSTJXEFk%2FVhN3N6fg4ahT9TK5diFwtyPIUokdYEJscqALQLws4gebovufvAtdPezOBENrTRrsx8hcw8TEbTKiYI2H9IsuzdYA4Z16OP2ZhpMS5HRjt0bC0mU2GYU04KXUwmQhY%2BjcKijDR2ppoKwHav5vWAEc5XaPvunnEZ%2FhcOX3i6zdg%2BSI7BGoM8WiNjtKWYAaNsKoCNcFITlxDp%2FEOAyonIiLA6zD2FoG0EMNEGaPBfclzNHhP2tJ%2FxIPkbWwyY3k0FG8dqRnMKVCsoQTN%2B9ALXyUAZi2N9Itx7p%2FyWBRZ4DhcpdsZotqsghgQV5mXXDWTvhv9PUmO4jTjocorZUHOtS%2FcviOTPa8YcTf5338gogSxrG42i54UlLaTXNwn7eYG1NNMyCa1d%2F6oaTz1HjoGlzOdfNmuupXVrkuSIUJ%2BiMioYa9Yi5c7t0N7jCHm9mMB3sjPKkvrBuPLpW7ZR60BZLBO%2FHDQXmAHaWETht5yiQ5OwDEMy4jshHUq6QOJphXeR00vlrR6SVkFCwultIZFQmXOkNOSceHgPflKLDoWbVygvxsZTEtH7Oi05Qf1sR3x53Wn2bDU6APhXGx4BqbACY2RvlJvClBAvjCj4oB0taUWlmLhR696S94DsOsj%2F7LLv3X%2BgpLXv7Pju9EFMepyJSIUjtBS0yIhyueRjs74cjQwawrVAA325ZQ4ZoDWtLz9SKNAN2FCzemdW1Pa5usofQYBeN4EzfdyeppeN6U%2FqWyvyJilueNWOhX1CYhTbfAJ2UYeANf1t4JXif9y0yv%2BhZlGPSCMNrYdIlgorqk8DylIV479h6QTwO4PqhMZfjMSCwyexr3kzYoURc6ozTHgIxW2C%2Fy%2FPV3NnK9ZLnaebjl64zTPomsaD3EB4BacytcG7lZkufBIR1jB9r2%2BJTD7HYcPm9I9J7UB%2FPA5KifBMqQfSyA2QKRuA9FJ6jqv03dC7HJ5n9bhtJY9v5IbiGlL7mGXh2tJsaL4sACJlfo%2Bs0aDZo8mSOlKb4snXav10XQC6FrAT9TVUC%2F7H4LaHTDOfY1Nj%2FObd%2BVK7K9bMBG1sUuMRSS9LwwvJgr5jQNH99VJCjrpAPInp6ZmeYm9AzB8esfM9BUsuRaPbUOYjGL8WpVtkD5OpwsD%2FUhfRPOTvP4YRrGIObPWAguStfAPD7T5siYpQSWnpD8b20h7Wg5ECgV71MRh5xwZJr0LAVh1dOcJUSMRJNlPVe5WBY5n3YNBb2zExiMR50wQAdXCVjsgSJFNidv9JgLEVLZWk%2Fl97QoSZSJQG7XzVA6EIrVtKLo3DYHw%2BIvN4WXe%2F6fEx9%2Fj3OtTXcUonPF6qnvsPj7jaSFZ7AVk0VqJWRhL2QPuhd4Xpplf9MqhLvE3Wh9%2B3z4jjf78OS7LI7r%2BwgF%2FdlnH9QNL7mChkBDTdEi9FycoB73DA%2F6V%2FodmlJY7tRA4BT0cHdslLmV6Y%2FKaSRb3zj437cWFqgx9%2F23j83ijNb1NSnTdOAyjjjwd%2BqNmUtlI6A%2BPQ2z2IFYP1uKHTRb2B%2Fd4%3D1blUoi7f">
 * State diagram</a>
 *
 * @param <ItemStack>
 */
@Singleton
public final class InventoryEditManager<ItemStack> extends Manager {
    @Inject
    private InventoryTriggerManager invManager;
    @Inject
    private IInventoryHandle handle;
    @Inject
    private IPluginManagement pluginManagement;
    @Inject
    private ITriggerLoader<InventoryTrigger> inventoryTriggerLoader;

    //map of player uuids to inventories representing the inventories currently being edited
    private final Map<UUID, InventoryTrigger> sessions = new HashMap<>();
    //inventories currently awaiting a save/discard/continue command
    private final Map<UUID, IInventory> suspended = new HashMap<>();

    @Inject
    private InventoryEditManager() {
        super();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void reload() {
    }

    @Override
    public void shutdown() {

    }

    public void startEdit(IPlayer player, String invTriggerName) {
        UUID u = player.getUniqueId();
        InventoryTrigger trigger = invManager.get(invTriggerName);

        if (trigger == null) {
            player.sendMessage("InventoryTrigger " + invTriggerName + " does not exist.");
            return;
        }

        if (isEditing(player)) {
            player.sendMessage("You are already editing an inventory.");
            return;
        }

        sessions.put(u, trigger);

        IItemStack[] items = trigger.getItems();
        IInventory inv = handle.createInventory(items.length, trigger.getInfo().getTriggerName());
        handle.setContents(inv, items);
        inv.open(player);
    }

    public boolean isEditing(IPlayer player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public void continueEdit(IPlayer player) {
        UUID u = player.getUniqueId();
        if (!suspended.containsKey(u)) {
            return;
        }
        IInventory inv = suspended.remove(u);
        player.openInventory(inv);
    }

    public void discardEdit(IPlayer player) {
        if (!sessions.containsKey(player.getUniqueId())) {
            return;
        }
        stopEdit(player);
        player.sendMessage("Discarded edits");
    }

    //helper method to remove the player from both maps
    public void stopEdit(IPlayer player) {
        UUID u = player.getUniqueId();
        sessions.remove(u);
        suspended.remove(u);
    }

    public void saveEdit(IPlayer player) {
        UUID u = player.getUniqueId();
        if (!sessions.containsKey(u)) {
            return;
        }
        IInventory inv = suspended.get(u);
        InventoryTrigger trigger = sessions.get(u);
        IItemStack[] items = handle.getContents(inv);

        replaceItems(trigger, items);
        stopEdit(player);
        player.sendMessage("Saved edits");
    }

    //helper method to replace all the items in an inventory trigger
    private void replaceItems(InventoryTrigger trigger, IItemStack[] items) {
        IItemStack[] triggerItems = trigger.getItems();
        for (int i = 0; i < triggerItems.length; i++) {
            triggerItems[i] = items[i];
        }

        inventoryTriggerLoader.save(trigger);
    }

    /**
     * @param player
     * @param inventory
     * @deprecated event handler. Must be called by either listener or tests.
     */
    @Deprecated
    public void onInventoryClose(IPlayer player, IInventory inventory) {
        UUID u = player.getUniqueId();
        if (!sessions.containsKey(u)) {
            return;
        }
        //filter out already suspended
        if (suspended.containsKey(u)) {
            return;
        }

        suspended.put(u, inventory);
        sendMessage(player);
    }

    private void sendMessage(IPlayer player) {
        pluginManagement.runCommandAsConsole(MESSAGE.replace("@p", player.getName()));
    }

    private static final char X = '\u2718';
    private static final char CHECK = '\u2713';
    private static final char PENCIL = '\u270E';
    private static final String JSON_FORMAT = "[\n" +
            "  \"\",\n" +
            "  {\n" +
            "    \"text\": \"%c Save\",\n" +
            "    \"bold\": true,\n" +
            "    \"underlined\": false,\n" +
            "    \"color\": \"green\",\n" +
            "    \"clickEvent\": {\n" +
            "      \"action\": \"run_command\",\n" +
            "      \"value\": \"/trg links inveditsave\"\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"text\": \"\\n\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"text\": \"%c Continue Editing\",\n" +
            "    \"bold\": true,\n" +
            "    \"underlined\": false,\n" +
            "    \"color\": \"yellow\",\n" +
            "    \"clickEvent\": {\n" +
            "      \"action\": \"run_command\",\n" +
            "      \"value\": \"/trg links inveditcontinue\"\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"text\": \"\\n\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"text\": \"%c Cancel\",\n" +
            "    \"bold\": true,\n" +
            "    \"underlined\": false,\n" +
            "    \"color\": \"red\",\n" +
            "    \"clickEvent\": {\n" +
            "      \"action\": \"run_command\",\n" +
            "      \"value\": \"/trg links inveditdiscard\"\n" +
            "    }\n" +
            "  }\n" +
            "]";
    private static final String MESSAGE = "tellraw @p " + String.format(JSON_FORMAT, CHECK, PENCIL, X);
}
