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
  var pt = {};
  var pg = {};
  var pog = {};
  var i = 0;
  this.teams.forEach(function(t) {
    t.forEach(function(p) {
      pt[p] = i;
      pg[p] = 0;
      pog[p] = 0;
    });
    i++;
  });
  this.scores.forEach(function(score) {
    if (score.team == pt[score.player])
      pg[score.player]++;
    else
      pog[score.player]++;
  });
  for (p in pg)
    emit(p, {games: 1, own_goals: pog[p], goals: pg[p]});
};

var reduce = function(key, values) {
  var o = {};
  o.games     = 0;
  o.goals     = 0;
  o.own_goals = 0;
  values.forEach(function(doc) {
      o.games     += doc.games;
      o.goals     += doc.goals;
      o.own_goals += doc.own_goals;
  });
  return o;
};

var finalize = function(key, value) {
  value.goals_per_game = value.goals / value.games;
  return value;
}

var query = {score: {$exists: true}, $where: "this.teams[0][0] != this.teams[0][1] && this.teams[1][0] != this.teams[1][1]"};

var mr = db.game.mapReduce(map, reduce, {finalize: finalize, query: query, out: {inline: 1}});

var players = [];
for (p in mr.results) {
  mr.results[p].value.player = mr.results[p]._id;
  players.push(mr.results[p].value);
}

printjson(players[0]);

