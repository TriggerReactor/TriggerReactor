var Bukkit = Java.type('org.bukkit.Bukkit');
var Entity = Java.type('org.bukkit.entity.Entity');
var Location = Java.type('org.bukkit.Location');
var BlockData = Java.type('org.bukkit.block.data.BlockData');

var validation = {
  overloads: [
    [
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
      { type: BlockData.class, name: 'data' },
    ],
    [
      { type: Location.class, name: 'location' },
      { type: BlockData.class, name: 'data' },
    ],
  ],
};

function FALLINGBLOCK(args) {
  var data;
  var location;

  if (overload === 0) {
    location = new Location(
      player instanceof Entity ? player.getWorld() : Bukkit.getWorld('world'),
      args[0],
      args[1],
      args[2]
    );
    data = args[3];
  } else if (overload === 1) {
    location = args[0];
    data = args[1];
  }

  location.getWorld().spawnFallingBlock(location, data);
}
