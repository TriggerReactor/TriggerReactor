var Entity = Java.type('org.bukkit.entity.Entity');
var Vector = Java.type('org.bukkit.util.Vector');

var validation = {
  overloads: [
    [
      { type: Entity.class, name: 'entity' },
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
    ],
  ],
};

function PUSH(args) {
  var entity = args[0];
  var motionX = args[1];
  var motionY = args[2];
  var motionZ = args[3];

  entity.setVelocity(
    new Vector(motionX.toFixed(2), motionY.toFixed(2), motionZ.toFixed(2))
  );
}
