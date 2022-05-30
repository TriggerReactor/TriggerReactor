var Bukkit = Java.type('org.bukkit.Bukkit');
var Entity = Java.type('org.bukkit.entity.Entity');
var Location = Java.type('org.bukkit.Location');
var Openable = Java.type('org.bukkit.block.data.Openable');

var validation = {
  overloads: [
    [{ type: Location.class, name: 'location' }],
    [
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
    ],
  ],
};

function DOOROPEN(args) {
  var location;

  if (overload === 0) location = args[0];
  else if (overload === 1)
    location = new Location(
      player instanceof Entity ? player.getWorld() : Bukkit.getWorld('world'),
      args[0],
      args[1],
      args[2]
    );

  var block = location.getBlock();
  var blockData = block.getBlockData();

  if (!(blockData instanceof Openable))
    throw new Error('This block is not openable block.');

  blockData.setOpen(true);
  block.setBlockData(blockData);
}
