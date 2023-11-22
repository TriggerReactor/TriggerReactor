var validation = {
    overloads: [[]],
};

function iscooldown(args) {
    var playerUuid = player.getUniqueId();
    return cooldown.isCooldown(playerUuid);
}
