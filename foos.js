Foos = {}
Foos_SRV = 'http://localhost:27080/';
Foos_DB = 'foos/game/';
Foos_URL = Foos_SRV + Foos_DB;

var crit = function(gameToken) { return 'criteria={"_id":{"$oid":"' + gameToken + '"}}'; }

Foos.games = {};

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
			console.log(data);
		}
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
	
	if (Foos.games[gameToken] != undefined) {
		var score = Foos.getScore(Foos.games[gameToken]);
		if (score[0] == 10 || score[1] == 10) {
			alert('game is over with score ' + score[0] + ' - ' + score[1]);
			return;
		}
	}
	
	var score = {
		$push: {
			scores: {
				team: team,
				player: player,
				position: position,
				date: {$date: new Date().getTime()}
			}
		}
	};
	var ops = '&newobj=' + JSON.stringify(score);
	
	$.post(Foos_URL + '_update', crit(gameToken) + ops, function() {

		if ($.isFunction(success)) {
			Foos.getGame(gameToken, success);
		}
		
	}, 'json');
}

Foos.getGame = function(gameToken, callback) {
	$.get(Foos_URL + '_find', crit(gameToken), function(data) {
		var game = data.results[0];
		Foos.games[gameToken] = game;
		if ($.isFunction(callback)) {
			callback(game);
		}
	}, 'json')
};

Foos.getScore = function(game) {
	var scores = [0,0];
	for (score_pos in game.scores) {
		var score = game.scores[score_pos];
		scores[score.team]++;
	}
	return scores;
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
