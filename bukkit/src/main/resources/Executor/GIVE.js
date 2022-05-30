var Player = Java.type('org.bukkit.entity.Player');
var ItemStack = Java.type('org.bukkit.inventory.ItemStack');

var validation = {
  overloads: [
    [{ type: ItemStack.class, name: 'item' }],
    [
      { type: Player.class, name: 'player' },
      { type: ItemStack.class, name: 'item' },
    ],
  ],
};

function GIVE(args) {
  var p = player;
  var item;

  if (overload === 0)
    item = args[0];
  else if (overload === 1) {
    p = args[0];
    item = args[1];
  }

  if (!(player instanceof Player)) return null;

  p.getInventory().addItem(item);
}
