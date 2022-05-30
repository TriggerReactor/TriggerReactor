var Bukkit = Java.type('org.bukkit.Bukkit');
var Entity = Java.type('org.bukkit.entity.Entity');
var Location = Java.type('org.bukkit.Location');
var ItemStack = Java.type('org.bukkit.inventory.ItemStack');

var validation = {
  overloads: [
    [
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
      { type: ItemStack.class, name: 'item' },
    ],
    [
      { type: Location.class, name: 'location' },
      { type: ItemStack.class, name: 'item' },
    ],
  ],
};

function ITEMFRAMEROTATE(args) {
  var location, item;

  if (overload === 0) {
    location = new Location(
      player instanceof Entity ? player.getWorld() : Bukkit.getWorld('world'),
      args[0],
      args[1],
      args[2]
    );
    item = args[3];
  } else if (overload === 1) {
    location = args[0];
    item = args[1];
  }

  for each(var entity in location.getWorld().getNearbyEntities(location, 1, 1, 1))
    if (entity.getType().getEntityClass().getSimpleName() === 'ItemFrame')
      entity.setRotation(entity.getRotation().rotateClockwise())
}
