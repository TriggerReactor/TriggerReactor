/*******************************************************************************
 *     Copyright (C) 2017 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
function SCOREBOARD(args) {
    //#SCOREBOARD "TEAM" <teamname> "ADD" <playername>
    //#SCOREBOARD "TEAM" <teamname> "REMOVE" <playername>
    //#SCOREBOARD "TEAM" <teamname> "PREFIX" <prefix>
    //#SCOREBOARD "TEAM" <teamname> "SUFFIX" <suffix>
    //#SCOREBOARD "TEAM" <teamname> "NAME" <name>
    //#SCOREBOARD "TEAM" <teamname> "INVISHARE" true/false
    //#SCOREBOARD "TEAM" <teamname> "TEAMDAMAGE" true/false

    //#SCOREBOARD "OBJ" <objectivename[:criteria]> "SET" <scorename> <value|null>
    //#SCOREBOARD "OBJ" <objectivename[:criteria]> "NAME" <name>
    //#SCOREBOARD "OBJ" <objectivename[:criteria]> "SLOT" <DisplaySlot>

    if (args.length != 4 && args.length != 5)
        throw new Error("Invalid parameters! [String, String, String, VALUE, [VALUE]]");

    var board = player.getScoreboard();
    if (board == null)
        board = Bukkit.getScoreboardManager().getNewScoreboard();

    if (args[0] == "TEAM") {
        var teamname = args[1];
        var operation = args[2];
        var value = args[3];

        if (typeof teamname !== "string")
            throw new Error(teamname + " is not String!");

        var team = board.getTeam(teamname);
        if (team == null)
            team = board.registerNewTeam(teamname);

        if (typeof operation !== "string")
            throw new Error(operation + " is not a String!");

        switch (operation) {
            case "ADD":
                if (typeof value !== "string")
                    throw new Error(value + " is not String!");

                team.addEntry(value);
                break;
            case "REMOVE":
                if (typeof value !== "string")
                    throw new Error(value + " is not String!");

                team.removeEntry(value);
                break;
            case "PREFIX":
                if (typeof value !== "string")
                    throw new Error(value + " is not String!");

                team.setPrefix(value);
                break;
            case "SUFFIX":
                if (typeof value !== "string")
                    throw new Error(value + " is not String!");

                team.setSuffix(value);
                break;
            case "NAME":
                if (typeof value !== "string")
                    throw new Error(value + " is not String!");

                team.setDisplayName(value)
                break;
            case "INVISHARE":
                if (typeof value !== "boolean")
                    throw new Error(value + " is not Boolean(true|false)!");

                team.setCanSeeFriendlyInvisibles(value);
                break;
            case "TEAMDAMAGE":
                if (typeof value !== "boolean")
                    throw new Error(value + " is not Boolean(true|false)!");

                team.setAllowFriendlyFire(value);
                break;
            default:
                throw new Error(operation + " is not valid operation!");
        }


    } else if (args[0] == "OBJ") {
        var objectiveName = args[1];

        if (typeof objectiveName !== "string")
            throw new Error(objectiveName + " is not String!");

        var split = objectiveName.split(":", 2);

        var objective = board.getObjective(split[0]);
        if (objective == null) {
            if (split.length == 2)
                objective = board.registerNewObjective(split[0], split[1])
            else
                objective = board.registerNewObjective(split[0], "dummy");
        }

        var operation = args[2];
        if (typeof operation !== "string")
            throw new Error(operation + " is not a String!");

        switch (operation) {
            case "SET":
                if (args.length != 5)
                    throw new Error("Invalid parameters! [String, String, String, String, Number]");

                var scorename = args[3];
                var value = args[4];

                if (typeof scorename !== "string")
                    throw new Error(scorename + " is not a String");

                if (value != null && typeof value !== "number")
                    throw new Error((typeof value) + " is not a Number");

                if (value == null)
                    board.resetScores(scorename);
                else
                    objective.getScore(scorename).setScore(value);
                break;
            case "NAME":
                var value = args[3];
                if (typeof value !== "string")
                    throw new Error(value + " is not a String");

                objective.setDisplayName(value);

                break;
            case "SLOT":
                var value = args[3];
                if (typeof value !== "string")
                    throw new Error(value + " is not a String");

                var DisplaySlot = Java.type('org.bukkit.scoreboard.DisplaySlot');
                var slotType = DisplaySlot.valueOf(value);

                objective.setDisplaySlot(slotType);

                break;
            default:
                throw new Error(operation + " is not valid operation!");
        }
    } else {
        throw new Error("Invalid parameters! " + args[0] + " is not a valid type.");
    }

    player.setScoreboard(board);
}