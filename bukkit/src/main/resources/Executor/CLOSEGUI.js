function CLOSEGUI(args) {
  if (!(player instanceof Player)) return null;

  player.closeInventory();
}
