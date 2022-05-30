var Player = Java.type('org.bukkit.entity.Player');

var validation = {
  overloads: [[], [{ type: Player.class, name: 'player' }]],
};

function CLEARCHAT(args) {
  var p = player;

  if (overload === 1) p = args[0];

  if (!(p instanceof Player)) return null;

  for (var i = 0; i < 128; i++) p.sendMessage('');
}
