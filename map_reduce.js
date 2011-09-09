//{
//	"_id" : ObjectId("4e538f1b97f2f77e9f000095"),
//	"date" : ISODate("2011-08-23T11:29:29.474Z"),
//	"score" : [
//		8,
//		10
//	],
//	"scores" : [
//		{
//			"date" : ISODate("2011-08-23T11:30:32.763Z"),
//			"player" : "Michael",
//			"position" : "w1",
//			"team" : 0
//		},
//		{
//			"date" : ISODate("2011-08-23T11:30:50.364Z"),
//			"player" : "Geries",
//			"position" : "b10",
//			"team" : 1
//		},
//    ...
//	],
//	"teams" : [
//		[
//			"Michael",
//			"Haseeb"
//		],
//		[
//			"Per-Anders",
//			"Geries"
//		]
//	]
//}

var map = function() {
  var players_to_team = {};
  var player_goals = {};
  var i = 0;
  this.teams.forEach(function(team) {
    team.forEach(function(player) {
      players_to_team[player] = i;
      player_goals[player] = 0;
    });
    i++;
  });
  this.scores.forEach(function(score) {
    if (score.team == players_to_team[score.player])
      player_goals[score.player]++;
  });
  for (player in player_goals)
    emit(player, {games: 1, goals: player_goals[player]});
};

var reduce = function(key, values) {
  var games = 0;
  var goals = 0;
  values.forEach(function(doc) {
      games += doc.games;
      goals += doc.goals;
  });
  return {games: games, goals: goals};
};

var mr = db.game.mapReduce(map, reduce, {out: {inline: 1}});

mr.results.forEach(function(player) {
  player.value.per_game = player.value.goals / player.value.games;
});

mr.results.sort(function(a, b) {
  return b.value.per_game - a.value.per_game;
}).forEach(printjson);

