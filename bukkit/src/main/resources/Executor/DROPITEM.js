var Bukkit = Java.type('org.bukkit.Bukkit');
var Entity = Java.type('org.bukkit.entity.Entity');
var ItemStack = Java.type('org.bukkit.inventory.ItemStack');
var Material = Java.type('org.bukkit.Material');
var Location = Java.type('org.bukkit.Location');

var validation = {
  overloads: [
    [
      { type: 'string', name: 'block' },
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
    ],
    [
      { type: 'string', name: 'block' },
      { type: 'number', name: 'amount' },
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
    ],
    [
      { type: 'string', name: 'block' },
      { type: Location.class, name: 'location' },
    ],
    [
      { type: 'string', name: 'block' },
      { type: 'number', name: 'amount' },
      { type: Location.class, name: 'location' },
    ],
    [
      { type: ItemStack.class, name: 'item' },
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
    ],
    [
      { type: ItemStack.class, name: 'item' },
      { type: Location.class, name: 'location' },
    ],
  ],
};

function DROPITEM(args) {
  var item, location;

  if (overload === 0) {
    item = new ItemStack(Material.valueOf(args[0]));
    location = new Location(
      player instanceof Entity ? player.getWorld() : Bukkit.getWorld('world'),
      args[1],
      args[2],
      args[3]
    );
  } else if (overload === 1) {
    item = new ItemStack(Material.valueOf(args[0]), args[1]);
    location = new Location(
      player instanceof Entity ? player.getWorld() : Bukkit.getWorld('world'),
      args[2],
      args[3],
      args[4]
    );
  } else if (overload === 2) {
    item = new ItemStack(Material.valueOf(args[0]));
    location = args[1];
  } else if (overload === 3) {
    item = new ItemStack(Material.valueOf(args[0]), args[1]);
    location = args[2];
  } else if (overload === 4) {
    item = args[0];
    location = new Location(
      player instanceof Entity ? player.getWorld() : Bukkit.getWorld('world'),
      args[1],
      args[2],
      args[3]
    );
  } else if (overload === 5) {
    item = args[0];
    location = args[1];
  }

  location.getWorld().dropItem(location, item);
}
