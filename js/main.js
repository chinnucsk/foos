window.addEventListener('load', function () {
   setTimeout(scrollTo, 0, 0, 1);
}, false);



$(function () {
   var token;
   var teams;
   var switched = false;
   var ownGoal = false;
   var players = [];

   var updateStats = function (game) {
      for (team in teams) {
         for (player in teams[team]) {
            $("#player" + team + player).text(teams[team][player]);
         }
      }

      var score = Foos.getScore(game);
      if (switched) { 
         score.reverse();
      }
      $("#score0").text(score[0]);
      $("#score1").text(score[1]);
      if (game.scores.length) {
         var lscore = game.scores[game.scores.length - 1];
         var msg = "";
         if (!Foos.isSelfGoal(game, lscore)) {
            msg = lscore.player + ' scores from ' + lscore.position;
         } else {
            msg = lscore.player + ' scores own goal from ' + lscore.position;
         }
         $("#log").text(msg);
      }
   }

   var goal = function (team, pos) {
      if (token == undefined) return;

      // find player
      var posColor = pos.substr(0, 1) == "w" ? 0 : 1;
      var posNum = pos.substr(1) * 1;
      var pl = teams[posColor];
      if (pl.length > 1){
         pl = pl[posNum < 3 ? 0 : 1]; 
      } else {
         pl = pl[0];
      }

      // reverse teams if switched
      if (switched) team = team == 0 ? 1 : 0;

      // own goal?
      if (ownGoal) {
         team = team == 0 ? 1 : 0;
         ownGoal = false;
         updateOG();
      }

      Foos.score(token, team, pl, pos, function (game) {
         var score = Foos.getScore(game);
         // switch teams
         if (!switched && (score[0] == 5 || score[1] == 5)) {
            switched = true;
            for (team in teams) teams[team].reverse();
            teams.reverse();
            var g = false;
            for (team in teams) for (p in teams[team]) if (teams[team][p] == 'Geries') g = true;
            alert("Switch sides" + (g ? " man, sides!" : "!"));
         }

         updateStats(game);

         // game finished
         if (score[0] == 10 || score[1] == 10) {
            var winning_team = score[0] == 10 ? 0 : 1;
            var losing_team = winning_team == 0 ? 1 : 0;
            var winners = game.teams[winning_team];
            alert("Winners are team " + winners[0] + " & " + winners[1]);

            var tally = Foos.tallyScores(game);
            var tallyList = [];
            for (t in tally) tallyList.push({name:t, score:tally[t]});
            tallyList.sort(function (a, b) {
               return a.score < b.score;
            });
            var tallyText = "";
            for (t in tallyList) tallyText += tallyList[t].name + "=" + tallyList[t].score + ", ";
            $("#log").text(tallyText.substr(0, tallyText.length - 2));

            $("#team" + losing_team + "0").val(game.teams[losing_team][1]);
            $("#team" + losing_team + "1").val(game.teams[losing_team][0]);
            $("#team" + winning_team + "0").val(game.teams[winning_team][0]);
            $("#team" + winning_team + "1").val(game.teams[winning_team][1]);

            $("#start-game").text("rematch!");
            $("#start-test-game").text("test rematch");
            $("#game-select").show("hide");
         }
      });

   };

   var undo = function () {
      Foos.undo(token, function (game) {
         var score = Foos.getScore(game);

         // switch sides?
         if (switched && ((score[0] >= score[1] && score[0] == 4) || (score[1] >= score[0] && score[1] == 4))) {
            switched = false;
            for (team in teams) teams[team].reverse();
            teams.reverse();
            alert("Switch back!");
         }

         // if we're undoing a winning goal, let the players know
         if ((score[0] >= score[1] && score[0] == 9) || (score[1] >= score[0] && score[1] == 9)) {
            $("#game-select").addClass("hide");
            alert("Game's not over yet!");
         }

         updateStats(game);
      });
   }

   var goalCaller = function (team, pos) {
      return function () {
         goal(team, pos);
      };
   }

   var setupCircles = function (color, team) {
      for (i = 0; i < 11; i++) {
         var pos = color + i;
         $("#" + pos).click(goalCaller(team, pos));
      }
   };

   var updateOG = function () {
      ownGoal ? $("#og").addClass("activate") : $("#og").removeClass("activate");
   };

   Foos.setHost('http://rouzbeh.videoplaza.org/foos/');
   setupCircles("w", 0);
   setupCircles("b", 1);

   var getPlayerLi = function (player) {
      return '<li class="player" data-player="' + player + '">' + player + '</li>';
   };

   Foos.getPlayers(function (players) {
      $.each(players, function (i, player) {
         $('ul#playerlist').append(getPlayerLi(player));
      });
      var x = [0, 1];
      for (p in players) {
         var pl = players[p];
         for (i in x) { 
            for (j in x) {
               $("#team" + i + j).append('<option value="' + pl + '">' + pl + '</option>');
            }
         }
      }
   });
   var startGameWithPlayers = function (players) {
      Foos.game([
         [players[0], players[1]],
         [players[2], players[3]]
      ], function (gameToken) {
            switched = false;
            token = gameToken;
            Foos.getGame(gameToken, function (game) {
               teams = game.teams;
               updateStats(game);
               //$("#log").text("...");
               $("#score0").text("0");
               $("#score1").text("0");
               $("#game-select").hide();
               $("#board").show();
            });
         });
   };
   if (window.location.hash) {
      var hash = window.location.hash.replace('#', '').split(',');
      FoosSetCL('game');
      $("ul#playerlist").addClass('hide');
      startGameWithPlayers(hash);
   }
   ;
   var isRematch = function () {
      return $("#start-game").text().match(/rematch/);
   };

   var getPlayers = function () {
      return [
         $("#team00n").val().length ? $("#team00n").val() : $("#team00").val(), $("#team01n").val().length ? $("#team01n").val() : $("#team01").val(), $("#team10n").val().length ? $("#team10n").val() : $("#team10").val(), $("#team11n").val().length ? $("#team11n").val() : $("#team11").val()
      ]
   };

   var setPlayers = function (selectedPlayers) {
      $("#team00").val(selectedPlayers[0]);
      $("#team01").val(selectedPlayers[1]);
      $("#team10").val(selectedPlayers[2]);
      $("#team11").val(selectedPlayers[3]);
   };

   var doTeamsOverlap = function (team0, team1) {
      var overlapping = false;
      $.each(team0, function (i, val) {
         if (team1.indexOf(val) >= 0) {
            overlapping = true;
         }
      });

      return overlapping;
   };

   var getBestTeams = function (selectedPlayers, skillFor) {
      var teamForSkill = {};
      for (var i = 0; i < selectedPlayers.length; i++) {
         for (var j = i + 1; j < selectedPlayers.length; j++) {
            teamForSkill[(skillFor[selectedPlayers[i]] + skillFor[selectedPlayers[j]]) / 2] = [i, j];
         }
      }

      var bestTeams = [];
      var lowestDelta = 99999.0;
      $.each(teamForSkill, function (key0, val0) {
         $.each(teamForSkill, function (key1, val1) {
            if (key0 == key1 || doTeamsOverlap(val0, val1)) {
               return;
            }

            var delta = Math.abs(key0 - key1);
            if (delta < lowestDelta) {
               bestTeams = [val0, val1];
               lowestDelta = delta;
            }
         });
      });

      return bestTeams;
   };

   var suggestTeams = function () {
      var NEW_TABLE_START_DATE = '1324584000000';
      var GET_TRUESKILL_URI = 'http://rouzbeh.videoplaza.org:8080/player?startDate=' + NEW_TABLE_START_DATE;

      // Reset new-school player selections
      players = [];

      var playerListElement = $("ul#playerlist");
      var suggestionElement = $("#team-suggestion");

      playerListElement.hide();
      $("#board").hide();
      $("#game-select").hide();
      suggestionElement.show();

      $.get(GET_TRUESKILL_URI, function (data) {
         var skillFor = {};
         $.each(data, function (i, val) {
            skillFor[val.name] = val.trueskill;
         });

         var selectedPlayers = getPlayers();
         var bestTeams = getBestTeams(selectedPlayers, skillFor);
         var t0p0 = selectedPlayers[bestTeams[0][0]];
         var t0p1 = selectedPlayers[bestTeams[0][1]];
         var t1p0 = selectedPlayers[bestTeams[1][0]];
         var t1p1 = selectedPlayers[bestTeams[1][1]];

         suggestionElement.empty();
         suggestionElement.append("<p>" + t0p0 + " and " + t0p1 + ": " +
            ((skillFor[t0p0] + skillFor[t0p1]) / 2) + "</p>");
         suggestionElement.append("<p>" + t1p0 + " and " + t1p1 + ": " +
            ((skillFor[t1p0] + skillFor[t1p1]) / 2) + "</p>");

         playerListElement.empty();
         playerListElement.append(getPlayerLi(t0p0));
         playerListElement.append(getPlayerLi(t0p1));
         playerListElement.append(getPlayerLi(t1p0));
         playerListElement.append(getPlayerLi(t1p1));
         playerListElement.show();
      },'json');
   };

   var startGame = function () {
      if (isRematch()) {
         alert("Losers switch!")
      }

      startGameWithPlayers(getPlayers());
   };
   $("#oldschool").click(function () {
      $("ul#playerlist").hide();
      $("#game-select").show();
      $("#oldschool").hide();
      $("#team-suggestion").hide();
   });
   $("ul#playerlist").click(function (e) {
      var player = $(e.target);
      //this is to unde selection of players 
      if ($('.number', player).length>0) {
         $('.number', player).remove();
         players.remove(players.indexOf(player.data('player')));
      } else {
         players.push(player.data('player'));
         player.append('<span class="number">' + players.length + '</span>');
      }
      if (players.length == 4) {
         var teamSuggestionElement = $("#team-suggestion");
         if (teamSuggestionElement.is(":visible")) {
            teamSuggestionElement.hide();
            $("ul#playerlist").hide();
            $("#oldschool").addClass('hide');
            FoosSetCL('game');
            startGameWithPlayers(players);
         } else {
            setPlayers(players);
            suggestTeams();
         }
      }
   });
   $("#suggest-teams").click(function () {
      suggestTeams();
   });
   $("#start-game").click(function () {
      FoosSetCL('game');
      startGame();
   });
   $("#start-test-game").click(function () {
      FoosSetCL('game_test');
      startGame();
   });
   $("#og").click(function () {
      ownGoal = !ownGoal;
      updateOG();
   });
   $("#undo").click(function () {
      undo();
   });
});