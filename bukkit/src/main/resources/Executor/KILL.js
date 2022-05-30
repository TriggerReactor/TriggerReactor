var Damageable = Java.type('org.bukkit.entity.Damageable');

var validation = {
  overloads: [[], [{ type: Damageable.class, name: 'player' }]],
};

function KILL(args) {
  var entity;

  if (overload === 0) entity = player;
  else if (overload === 1) entity = args[0];

  if (entity === null) return null;

  entity.setHealth(0);
}
