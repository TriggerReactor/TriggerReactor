var Player = Java.type('org.bukkit.entity.Player');
var ChatColor = Java.type('org.bukkit.ChatColor');

validation = {
  overloads: [
    [],
    [{ type: Player.class, name: 'player' }],
    [{ type: 'string', name: 'reason' }],
    [
      { type: Player.class, name: 'player' },
      { type: 'string', name: 'reason' },
    ],
  ],
};

function KICK(args) {
  var p = player;
  var reason = "&c[TR] You've been kicked from the server.";

  if (overload === 1) p = args[0];
  else if (overload === 2) reason = args[0];
  else if (overload === 3) {
    p = args[0];
    reason = args[1];
  }

  if (p === null) return null;

  reason = ChatColor.translateAlternateColorCodes('&', reason);

  player.kickPlayer(reason);
}
