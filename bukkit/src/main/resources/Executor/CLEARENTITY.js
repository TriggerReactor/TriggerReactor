var validation = {
  overloads: [[{ type: 'number', minimum: 0, name: 'radius' }]],
};

function CLEARENTITY(args) {
  var radius = args[0];

  if (!(p instanceof Player)) return null;

  for each (var entity in player.getNearbyEntities(args[0], args[0], args[0]))
    entity.remove();
}
