var Bukkit = Java.type('org.bukkit.Bukkit');
var ChatColor = Java.type('org.bukkit.ChatColor');

function BROADCAST(args) {
  var PlaceholderAPI;
  var msg = '';

  for each (var str in args)
    msg += str;
  msg = ChatColor.translateAlternateColorCodes('&', msg);

  if (Bukkit.getPluginManager().isPluginEnabled('PlaceholderAPI'))
    PlaceholderAPI = Java.type('me.clip.placeholderapi.PlaceholderAPI');

  for each (var p in Bukkit.getOnlinePlayers())
    p.sendMessage(PlaceholderAPI ? PlaceholderAPI.setPlaceholders(p, msg) : msg);
}
