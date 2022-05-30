var Player = Java.type('org.bukkit.entity.Player');

var validation = {
  overloads: [
    [{ type: 'number', minimum: 0, name: 'seconds' }],
    [
      { type: Player.class, name: 'player' },
      { type: 'number', minimum: 0, name: 'seconds' },
    ],
  ],
};

function BURN(args) {
  var p = player;
  var seconds;

  if (overload === 0) seconds = args[0];
  else if (overload === 1) {
    p = args[0];
    seconds = args[1];
  }

  if (!(p instanceof Player)) return null;

  p.setFireTicks(seconds * 20);
}
