var ChatColor = Java.type('org.bukkit.ChatColor');
var ChatMessageType = Java.type('net.md_5.bungee.api.ChatMessageType');
var Player = Java.type('org.bukkit.entity.Player');
var TextComponent = Java.type('net.md_5.bungee.api.chat.TextComponent');

var validation = {
  overloads: [
    [{ type: 'string', name: 'message' }],
    [
      { type: Player.class, name: 'player' },
      { type: 'string', name: 'message' },
    ],
  ],
};

function ACTIONBAR(args) {
  var p = player;
  var msg;

  if (overload === 0) msg = args[0];
  else if (overload === 1) {
    p = args[0];
    msg = args[1];
  }

  if (!(p instanceof Player)) return null;

  msg = ChatColor.translateAlternateColorCodes('&', msg);

  p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
}
