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

function LIGHTNING(args) {
  var location;

  if (overload === 0) location = args[0];
  else if (overload === 1)
    location = new Location(
      player instanceof Entity ? player.getWorld() : Bukkit.getWorld('world'),
      args[0],
      args[1],
      args[2]
    );

  location.getWorld().strikeLightning(location);
}
