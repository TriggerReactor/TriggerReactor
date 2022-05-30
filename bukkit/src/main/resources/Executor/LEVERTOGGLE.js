var Bukkit = Java.type('org.bukkit.Bukkit');
var Entity = Java.type('org.bukkit.entity.Entity');
var Location = Java.type('org.bukkit.Location');
var Powerable = Java.type('org.bukkit.block.data.Powerable');

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

function LEVERTOGGLE(args) {
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

  if (!(blockData instanceof Powerable))
    throw new Error('This block is not lever.');

  blockData.setPowered(!blockData.isPowered());
  block.setBlockData(blockData);
}
