Foos = {}
Foos_SRV = '';
Foos_DB = 'foos';
Foos_CL = 'game'
Foos_PLCL = 'player'
Foos_URL = Foos_SRV + Foos_DB + '/' + Foos_CL + '/';
Foos_PLURL = Foos_SRV + Foos_DB + '/' + Foos_PLCL + '/';

var FoosSetCL = function(cl) {
	Foos_CL = cl;
	Foos_URL = Foos_SRV + Foos_DB + '/' + Foos_CL + '/';
	Foos_PLURL = Foos_SRV + Foos_DB + '/' + Foos_PLCL + '/';
}

Foos.setHost = function(host) {
  Foos_SRV = host;
  FoosSetCL(Foos_CL);
};

Foos.games = {};

var _foos = {
	crit: function(gameToken) { return 'criteria={"_id":{"$oid":"' + gameToken + '"}}'; },
	update: function(gameToken, opsObj, callback) {
		var ops = '&newobj=' + JSON.stringify(opsObj);
		$.post(Foos_URL + '_update', _foos.crit(gameToken) + ops, function() {
		
			_foos.getGame(gameToken, callback);
		
		}, 'json');
	},
	getGame: function(gameToken, callback) {
		$.get(Foos_URL + '_find', _foos.crit(gameToken), function(data) {
		
			var game = data.results[0];
			Foos.games[gameToken] = game;
		
			if ($.isFunction(callback)) {
				callback(game);
			}
		
		}, 'json')
	}
}

Foos.game = function(teams, success) {
	if (teams.length != 2) {
		alert('must have 2 teams');
		return;
	}
	
	var docs = [{
			teams: teams,
			scores: [],
			date: {$date: new Date().getTime()}
		}
	];
	$.post(Foos_URL + '_insert', 'docs=' + JSON.stringify(docs), function(data) {
		if ($.isFunction(success)) {
			success(data.oids[0].$oid);
		}
		// save player names
		setTimeout(function() {
			for (team in teams) for (player in teams[team]) if (teams[team][player].length)
				$.post(Foos_PLURL + '_insert', 'docs=' + JSON.stringify({name:teams[team][player]}));
		},50);
	},'json');
}

Foos.score = function(gameToken, team, player, position, success) {
	if (gameToken == undefined) {
		alert('must specify gameToken');
		return;
	}
	
	if (team == undefined) {
		alert('must specify team');
		return;
	}
	
	var teams;
	if (Foos.games[gameToken] != undefined) {
		var game = Foos.games[gameToken];
		var score = Foos.getScore(game);
		if (score[0] == 10 || score[1] == 10) {
			alert('game is already over with score ' + score[0] + ' - ' + score[1]);
			return;
		}
		teams = game.teams;
	}
	
	var ops = {
		$push: {
			scores: {
				team: team,
				player: player,
				position: position,
				date: {$date: new Date().getTime()}
			}
		}
	};
	if (teams) {
		if (teams[team].indexOf(player) == -1)
			ops.$push.scores.ownGoal = true;
	}
	
	_foos.update(gameToken, ops, function(game) {
		var score = Foos.getScore(game);
		if (score[0] == 10 || score[1] == 10) {
			// game ended
			var ops = {
				$set: {
					score: score
				}
			}
			_foos.update(gameToken, ops);
		}
		
		if ($.isFunction(success)) {
			success(game);
		}
	});
}

Foos.undo = function(gameToken, success) {
  if (gameToken == undefined) {
    alert('must specify gameToken');
    return;
  }

  var game = Foos.games[gameToken];
  if (game == undefined)
    return;

  var score = Foos.getScore(game);
  if (score[0] == 0 && score[1] == 0) {
    alert('Cannot undo when score is 0-0');
    return;
  }

  var ops = { $pop: { scores: 1 } };

  // The game can never be over after an undo, so mark it as incomplete
  ops.$unset = { score: 1 };

  _foos.update(gameToken, ops, function(game) {
    if ($.isFunction(success)) {
      success(game);
    }
  });
}

Foos.getGame = _foos.getGame;

Foos.getPlayers = function(callback) {
	if ($.isFunction(callback)) {
		$.get(Foos_PLURL + '_find?batch_size=1000', function(data) {
			var players = [];
			for (result in data.results) {
				players.push(data.results[result].name);
			}
			players.sort();
			callback(players);
		},'json');
	}
}

Foos.getScore = function(game) {
	var scores = [0,0];
	for (score_pos in game.scores) {
		var score = game.scores[score_pos];
		scores[score.team]++;
	}
	return scores;
}

Foos.tallyScores = function(game) {
	var tally = {};
	for (score in game.scores) {
		var pl = game.scores[score].player;
		if (!tally[pl]) tally[pl] = 0;
		tally[pl] += 1;
	}
	return tally;
}

Foos.isSelfGoal = function(game, score) {
	if (score.player == undefined) {
		return false;
	}
	var team = -1;
	if (game.teams[0].indexOf(score.player) != -1) team = 0;
	if (game.teams[1].indexOf(score.player) != -1) team = 1;
	if (score.team != team) return true;
	return false;
}
