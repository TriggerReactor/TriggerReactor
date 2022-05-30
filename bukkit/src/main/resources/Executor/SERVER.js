var validation = {
  overloads: [[{ type: 'string', name: 'server' }]],
};

function SERVER(args) {
  var server = args[0];

  if (player === null) return null;

  plugin.getBungeeHelper().sendToServer(player, server);
}
