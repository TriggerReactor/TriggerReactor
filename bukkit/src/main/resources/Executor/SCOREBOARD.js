/*******************************************************************************
 *     Copyright (C) 2017 wysohn
 *     Copyright (C) 2022 Ioloolo
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

var DisplaySlot = Java.type('org.bukkit.scoreboard.DisplaySlot');

var validation = {
  overloads: [
    [
      { type: 'string', name: 'type', matches: 'OBJ' },
      { type: 'string', name: 'boardName' },
      { type: 'string', name: 'subType', matches: 'SET' },
      { type: 'string', name: 'scoreName' },
      { type: 'int', name: 'score' }
    ],
    [
      { type: 'string', name: 'type', matches: 'OBJ' },
      { type: 'string', name: 'boardName' },
      { type: 'string', name: 'subType', matches: 'REMOVE' },
      { type: 'string', name: 'scoreName' }
    ],
    [
      { type: 'string', name: 'type', matches: 'OBJ' },
      { type: 'string', name: 'boardName' },
      { type: 'string', name: 'subType', matches: 'NAME' },
      { type: 'string', name: 'displayName' }
    ],
    [
      { type: 'string', name: 'type', matches: 'OBJ' },
      { type: 'string', name: 'boardName' },
      { type: 'string', name: 'subType', matches: 'SLOT' },
      { type: 'string', name: 'slot' }
    ],
    [
      { type: 'string', name: 'type', matches: 'TEAM' },
      { type: 'string', name: 'teamName' },
      { type: 'string', name: 'subType', matches: 'ADD' },
      { type: 'string', name: 'entry' }
    ],
    [
      { type: 'string', name: 'type', matches: 'TEAM' },
      { type: 'string', name: 'teamName' },
      { type: 'string', name: 'subType', matches: 'REMOVE' },
      { type: 'string', name: 'entry' }
    ],
    [
      { type: 'string', name: 'type', matches: 'TEAM' },
      { type: 'string', name: 'teamName' },
      { type: 'string', name: 'subType', matches: 'PREFIX' },
      { type: 'string', name: 'prefix' }
    ],
    [
      { type: 'string', name: 'type', matches: 'TEAM' },
      { type: 'string', name: 'teamName' },
      { type: 'string', name: 'subType', matches: 'SUFFIX' },
      { type: 'string', name: 'suffix' }
    ],
    [
      { type: 'string', name: 'type', matches: 'TEAM' },
      { type: 'string', name: 'teamName' },
      { type: 'string', name: 'subType', matches: 'NAME' },
      { type: 'string', name: 'newTeamName' }
    ],
    [
      { type: 'string', name: 'type', matches: 'TEAM' },
      { type: 'string', name: 'teamName' },
      { type: 'string', name: 'subType', matches: 'INVISHARE' },
      { type: 'boolean', name: 'isInvishare' }
    ],
    [
      { type: 'string', name: 'type', matches: 'TEAM' },
      { type: 'string', name: 'teamName' },
      { type: 'string', name: 'subType', matches: 'TEAMDAMAGE' },
      { type: 'boolean', name: 'isTeamDamage' }
    ]
  ]
};

function SCOREBOARD(args) {
  var board = player.getScoreboard();
  if (!board) board = Bukkit.getScoreboardManager().getNewScoreboard();

  if (0 <= overload && overload <= 3) {
    var objectiveName = args[1].split(':')[0];
    var objectiveCriteria = args[1].split(':', 2)[1];

    var objective = board.getObjective(objectiveName);
    if (!objective)
      objective = board.registerNewObjective(objectiveName, objectiveCriteria);

    if (0 <= overload && overload <= 1) {
      var score = objective.getScore(args[3]);

      if (overload === 0) score.setScore(args[4]);
      else score.setScore(null);
    } else if (overload === 2) objective.setDisplayName(args[3]);
    else if (overload === 3)
      objective.setDisplaySlot(DisplaySlot.valueOf(args[3].toUpperCase()));
  } else {
    var team = board.getTeam(args[1]);
    if (!team) team = board.registerNewTeam(args[1]);

    if (overload === 4) team.addEntry(args[3]);
    else if (overload === 5) team.removeEntry(args[3]);
    else if (overload === 6) team.setPrefix(args[3]);
    else if (overload === 7) team.setSuffix(args[3]);
    else if (overload === 8) team.setDisplayName(args[3]);
    else if (overload === 9) team.setCanSeeFriendlyInvisibles(args[3]);
    else if (overload === 10) team.setAllowFriendlyFire(args[3]);
  }

  player.setScoreboard(board);
}
