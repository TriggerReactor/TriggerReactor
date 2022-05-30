var validation = {
  overloads: [[{ type: 'string', name: 'permission' }]],
};

function PERMISSION(args) {
  var permission = args[0];

  if (player === null) return null;

  if (permission.startsWith('-')) vault.revoke(player, permission.substring(1));
  else vault.permit(player, permission);
}
