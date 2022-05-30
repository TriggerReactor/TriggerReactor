var Bukkit = Java.type('org.bukkit.Bukkit');

function CMDCON(args) {
  Bukkit.dispatchCommand(Bukkit.getConsoleSender(), args[0]);
}
