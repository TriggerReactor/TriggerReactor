var Player = Java.type('org.bukkit.entity.Player');

var validation = {
  overloads: [
    [{ type: 'string', name: 'name' }],
    [
      { type: Player.class, name: 'player' },
      { type: 'string', name: 'name' },
    ],
  ],
};

function GUI(args) {
  var p = player;
  var name;

  if (overload === 0)
    name = args[0];
  else if (overload === 1) {
    p = args[0];
    name = args[1];
  }

  if (!(p instanceof Player)) return null;

  if (plugin.getInvManager().openGUI(p, name) === null)
    throw new Error('No such Inventory Trigger named ' + name);
}
