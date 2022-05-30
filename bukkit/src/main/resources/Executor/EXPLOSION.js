var Bukkit = Java.type('org.bukkit.Bukkit');
var Entity = Java.type('org.bukkit.entity.Entity');
var Location = Java.type('org.bukkit.Location');

var validation = {
  overloads: [
    [
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
    ],
    [
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
      { type: 'number', name: 'power' },
    ],
    [
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
      { type: 'boolean', name: 'fire' },
    ],
    [
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
      { type: 'number', name: 'power' },
      { type: 'boolean', name: 'fire' },
    ],
    [{ type: Location.class, name: 'location' }],
    [
      { type: Location.class, name: 'location' },
      { type: 'number', name: 'power' },
    ],
    [
      { type: Location.class, name: 'location' },
      { type: 'boolean', name: 'fire' },
    ],
    [
      { type: Location.class, name: 'location' },
      { type: 'number', name: 'power' },
      { type: 'boolean', name: 'fire' },
    ],
  ],
};

function EXPLOSION(args) {
  var location, power, fire;

  if (0 <= overload && overload <= 3)
    location = location = new Location(
      player instanceof Entity ? player.getWorld() : Bukkit.getWorld('world'),
      args[0],
      args[1],
      args[2]
    );
  else location = args[0];

  if (overload === 0 || overload === 4) {
    power = 4;
    fire = false;
  } else if (overload === 1) {
    power = args[3];
    fire = false;
  } else if (overload === 2) {
    power = 4;
    fire = args[3];
  } else if (overload === 3) {
    power = args[3];
    fire = args[4];
  } else if (overload === 5) {
    power = args[1];
    fire = false;
  } else if (overload === 6) {
    power = 4;
    fire = args[1];
  } else if (overload === 7) {
    power = args[1];
    fire = args[2];
  }

  location.getWorld().createExplosion(location, power, fire);
}
