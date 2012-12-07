package com.videoplaza.foosball;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.videoplaza.foosball.model.Game;
import com.videoplaza.foosball.model.Player;
import com.videoplaza.foosball.skill.FoosballRating;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author jakob
 */
public class FoosballDB {

   private static final Date DEFAULT_START_DATE = new Date(0);
   private static final Date DEFAULT_END_DATE = new Date(Long.MAX_VALUE);
   private static final int DEFAULT_MIN_REQUIRED_GAMES = 0;

   private DB db;

   private SortedMap<Date, Game> processedGames;
   private Map<String, Player> players;
   private Map<String, Player> playersExceptLeaderboard;

   public static void main(String[] args) {
      String startDate = args.length >= 1 ? args[0] : null;
      String endDate = args.length >= 2 ? args[1] : null;
      String minRequiredGames = args.length >= 3 ? args[2] : null;

      FoosballDB db = new FoosballDB("rouzbeh.videoplaza.org", "foos");
      System.out.println(db.recalculate(startDate, endDate, minRequiredGames, null).toJson());
   }

   public FoosballDB(String host, String database) {
      try {
         Mongo mongo = new Mongo(host);
         db = mongo.getDB(database);
      } catch (UnknownHostException e) {
         e.printStackTrace();
      } catch (MongoException e) {
         e.printStackTrace();
      }
   }

   public PlayerStats recalculate(String startDateStr, String endDateStr, String minRequiredGamesStr, String leaderboardStartDateStr) {
      Date startDate = createDate(startDateStr, DEFAULT_START_DATE);
      Date endDate = createDate(endDateStr, DEFAULT_END_DATE);
      Date leaderboardStartDate = createDate(leaderboardStartDateStr, null);
      int minRequiredGames = createInt(minRequiredGamesStr, DEFAULT_MIN_REQUIRED_GAMES);

      System.err.println(
         "Recalculating TrueSkill for players with at least " + minRequiredGames
            + " game(s) played between "
            + startDate.toString()
            + " and "
            + endDate.toString()
            + (leaderboardStartDate == null ? "" : "; leaderboard starting " + leaderboardStartDate.toString())
      );

      players = new HashMap<String, Player>();
      playersExceptLeaderboard = new HashMap<String, Player>();

      processedGames = new TreeMap<Date, Game>();
      for (Game game : processGames(startDate, endDate, leaderboardStartDate))
         processedGames.put(game.getStarted(), game);

      FoosballRating ratings = new FoosballRating(players.values());

      for (Entry<Date, Game> entry : processedGames.entrySet()) {
         Game game = entry.getValue();

         if (isFourPlayerGame(game)) {
            recordGame(players, ratings, game);

            if (leaderboardStartDate != null) {
               if (game.getStarted().before(leaderboardStartDate))
                  recordGame(playersExceptLeaderboard, ratings, game);
               else
                  System.err.println(
                     "leaderboard from " + leaderboardStartDate.toString() + ": skipping game starting at " + game.getStarted().toString()
                  );
            }
         } else {
            System.err.println("Disqualifying non-4-player game: " + game);
         }
      }

      updatePlayers(new ArrayList<Player>(players.values()));

      return new PlayerStats(playerStats(), minRequiredGames);
   }

   public String makeMatch(String startDate, String endDate, final List<String> playerNames) {
      PlayerStats stats = recalculate(startDate, endDate, null, null);
      Player[] playersInGame = FluentIterable.from(stats.getPlayers())
         .filter(new Predicate<Player>() {
            @Override
            public boolean apply(Player player) {
               return playerNames.contains(player.getName());
            }
         })
         .toArray(Player.class);

      return new Matches(playersInGame[0], playersInGame[1], playersInGame[2], playersInGame[3])
         .toJson();
   }

   private List<Player> playerStats() {
      List<Player> stats = new ArrayList<Player>();

      for (Entry<String, Player> entry : players.entrySet())
         stats.add(entry.getValue().minus(playersExceptLeaderboard.get(entry.getKey())));

      Collections.sort(stats, new Comparator<Player>() {
         @Override
         public int compare(Player a, Player b) {
            return a.getTrueSkill() > b.getTrueSkill() ? -1 : 1;
         }
      });

      return stats;
   }

   private boolean isFourPlayerGame(Game game) {
      Set<String> playersInThisGame = new HashSet<String>();
      playersInThisGame.addAll(Arrays.asList(game.getHomeTeam()));
      playersInThisGame.addAll(Arrays.asList(game.getAwayTeam()));

      return playersInThisGame.size() == 4;
   }

   public List<Game> processGames(Date startDate, Date endDate, Date leaderboardStartDate) {
      DBCollection collection = db.getCollection("game");
      DBCursor cursor = collection.find();
      List<Game> result = new ArrayList<Game>();
      long count = 0;
      for (DBObject object : cursor) {
         try {
            Date started = (Date) object.get("date");
            if (started.before(startDate) || started.after(endDate))
               continue;

            List<Integer> score = (List) object.get("score");
            List<DBObject> scores = (List) object.get("scores");
            List teams = (List) object.get("teams");
            List<String> homeTeam = (List) teams.get(0);
            List<String> awayTeam = (List) teams.get(1);
            result.add(new Game(started, homeTeam.get(0), homeTeam.get(1), awayTeam.get(0), awayTeam.get(1), score.get(0), score.get(1)));
            List<String> winningTeam = score.get(0) == 10 ? homeTeam : awayTeam;
            List<String> losingTeam = score.get(0) != 10 ? homeTeam : awayTeam;

            boolean isBeforeLeaderboardStart = leaderboardStartDate != null && started.before(leaderboardStartDate);

            for (String name : winningTeam) {
               getPlayer(name).win();

               if (isBeforeLeaderboardStart)
                  getPlayerExceptLeaderboard(name).win();
            }

            for (String name : losingTeam) {
               getPlayer(name).lose();

               if (isBeforeLeaderboardStart)
                  getPlayerExceptLeaderboard(name).lose();
            }

            for (DBObject s : scores) {
               String name = (String) s.get("player");
               getPlayer(name).score();

               if (isBeforeLeaderboardStart)
                  getPlayerExceptLeaderboard(name).score();
            }
         } catch (Exception _) {
            // Nothing to do
         }
         count++;
      }

      System.err.println("Successfully converted " + count + " games.");

      return result;
   }

   public SortedMap<Date, Game> getProcessedGames() {
      return processedGames;
   }

   private Player getPlayer(String name) {
      return getPlayerFrom(players, name);
   }

   private Player getPlayerExceptLeaderboard(String name) {
      return getPlayerFrom(playersExceptLeaderboard, name);
   }

   public void updatePlayers(List<Player> players) {
      DBCollection collection = db.getCollection("player");
      for (Player player : players) {
         BasicDBObject query = new BasicDBObject();
         query.put("name", player.getName());
         BasicDBObject fields = new BasicDBObject();

         fields.put("trueskill", player.getTrueSkill());
         fields.put("mu", player.getSkill());
         fields.put("sigma", player.getUncertainty());

         BasicDBObject operation = new BasicDBObject();
         operation.put("$set", fields);

         collection.update(query, operation);
      }
   }

   private Player getPlayerFrom(Map<String, Player> map, String name) {
      Player player = map.get(name);
      if (player == null) {
         player = new Player(name);
         map.put(name, player);
      }

      return player;
   }

   public static Date createDate(String str, Date fallback) {
      try {
         return new Date(Long.parseLong(str));
      } catch (Exception _) {
         return fallback;
      }
   }

   private int createInt(String str, int fallback) {
      try {
         return Integer.valueOf(str);
      } catch (Exception _) {
         return fallback;
      }
   }

   private static void recordGame(Map<String, Player> players, FoosballRating ratings, Game game) {
      Map<String, Double> deltaMu = new HashMap<String, Double>();
      Map<String, Double> deltaSigma = new HashMap<String, Double>();
      for (Player p : game.getPlayers(players)) {
         deltaMu.put(p.getName(), p.getSkill());
         deltaSigma.put(p.getName(), p.getUncertainty());
      }

      ratings.recordMatch(players.get(game.getHomeTeam()[0]), players.get(game.getHomeTeam()[1]), players.get(game.getAwayTeam()[0]),
         players.get(game.getAwayTeam()[1]), game.getHomeScore(), game.getAwayScore());

      for (Player p : game.getPlayers(players)) {
         game.getDeltaMu().put(p.getName(), p.getSkill() - deltaMu.get(p.getName()));
         game.getDeltaSigma().put(p.getName(), p.getUncertainty() - deltaSigma.get(p.getName()));
      }

   }
}
