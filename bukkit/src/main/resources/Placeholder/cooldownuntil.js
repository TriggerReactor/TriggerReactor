var validation = {
    overloads: [[], [{ type: "string", name: "unit" }]],
};

var TimeUnit = Java.type("java.util.concurrent.TimeUnit");

function mapTimeUnit(unit) {
    switch (unit) {
        case "milliseconds":
            return TimeUnit.MILLISECONDS;
        case "seconds":
            return TimeUnit.SECONDS;
        case "minutes":
            return TimeUnit.MINUTES;
        case "hours":
            return TimeUnit.HOURS;
        case "days":
            return TimeUnit.DAYS;
        default:
            throw new Error("Unknown time unit: " + unit);
    }
}

function cooldownuntil(args) {
    var unit = "milliseconds";
    if (overload === 1) {
        unit = args[0];
    }

    var timeUnit = mapTimeUnit(unit);
    var playerUuid = player.getUniqueId();

    return cooldown.getCooldown(playerUuid, timeUnit);
}
