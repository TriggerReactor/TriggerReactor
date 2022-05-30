var PotionEffectType = Java.type('org.bukkit.potion.PotionEffectType');

var validation = {
  overloads: [
    [],
    [{ type: 'string', name: 'effect' }],
    [{ type: PotionEffectType.class, name: 'effect' }],
  ],
};

function CLEARPOTION(args) {
  var effect;

  if (!(player instanceof Player)) return null;

  if (overload === 0) {
    for each (var effect in player.getActivePotionEffects())
      player.removePotionEffect(effect.getType());

    return null;
  } else if (overload === 1) {
    effect = PotionEffectType.getByName(args[0]);
  } else if (overload === 2) {
    effect = args[0];
  }

  player.removePotionEffect(effect);
}
