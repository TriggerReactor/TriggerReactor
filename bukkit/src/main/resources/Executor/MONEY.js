var validation = {
  overloads: [[{ type: 'number', name: 'money' }]],
};

function MONEY(args) {
  if (player === null) return null;

  if (args[0] > 0) vault.give(player, args[0]);
  else vault.take(player, -args[0]);
}
