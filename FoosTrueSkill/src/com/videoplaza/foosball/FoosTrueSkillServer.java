package com.videoplaza.foosball;

import com.videoplaza.foosball.model.Game;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

public class FoosTrueSkillServer extends HttpServlet {
   private static final long serialVersionUID = 1L;

   private static final int SERVER_PORT = 8080;
   private static final String SERVER_CONTEXT_PATH = "/";
   private static final String SERVER_PATH_SPEC = SERVER_CONTEXT_PATH + "*";

   private static final String GAME_STATS_CONTENT_TYPE = "text/plain";
   private static final String JSON_CONTENT_TYPE = "application/json";

   private static final String GAME_STATS_DELIMITER = "\t";
   private static final String GAME_STATS_VERSUS = "vs";

   private static final String MONGO_HOSTNAME = "foos.videoplaza.org";
   private static final String MONGO_DATABASE = "foos";

   private FoosballDB db = new FoosballDB(MONGO_HOSTNAME, MONGO_DATABASE);

   private final NumberFormat nf = NumberFormat.getInstance();

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      String startDate = req.getParameter(Param.START_DATE);
      String endDate = req.getParameter(Param.END_DATE);
      String leaderboardStartDate = req.getParameter(Param.LEADERBOARD_START_DATE);
      String minRequiredGames = req.getParameter(Param.MIN_REQ_GAMES);

      Cors.addHeaders(req, resp);

      try {
         switch (RequestType.from(req)) {
         case GAME:
            printGameStats(resp);
            break;

         case MATCHMAKER:
            printJson(resp, db.makeMatch(startDate, endDate, getPlayers(req)));
            break;

         case PLAYER:
            printJson(resp, db.recalculate(startDate, endDate, minRequiredGames, leaderboardStartDate).toJson());
            break;
         }
      } catch (Exception e) {
         e.printStackTrace();
         resp.sendError(500, e.toString());
      }
   }

   private List<String> getPlayers(HttpServletRequest req) {
      String[] players = req.getParameterValues(Param.PLAYERS);

      int numPlayers = players == null ? 0 : players.length;
      if (numPlayers != 4)
         throw new IllegalArgumentException("4 players required for matchmaking; received " + numPlayers);

      return Arrays.asList(players);
   }

   private void printJson(HttpServletResponse resp, String output) throws IOException {
      resp.setContentType(JSON_CONTENT_TYPE);
      resp.getWriter().print(output);
   }

   private void printGameStats(HttpServletResponse resp) throws IOException {
      resp.setContentType(GAME_STATS_CONTENT_TYPE);

      PrintWriter writer = resp.getWriter();
      SortedMap<Date, Game> games = db.getProcessedGames();
      for (Entry<Date, Game> entry : games.entrySet()) {
         Game game = entry.getValue();

         writer.println(entry.getKey() + GAME_STATS_DELIMITER + game.getHomeScore() + GAME_STATS_DELIMITER + game.getAwayScore());
         for (String player : game.getHomeTeam())
            writer.println(player + GAME_STATS_DELIMITER + num(game.getDeltaMu(), player) + GAME_STATS_DELIMITER + num(game.getDeltaSigma(), player));

         writer.println(GAME_STATS_VERSUS);

         for (String player : game.getAwayTeam())
            writer.println(player + GAME_STATS_DELIMITER + num(game.getDeltaMu(), player) + GAME_STATS_DELIMITER + num(game.getDeltaSigma(), player));

         writer.println();
      }
   }

   private String num(Map<String, Double> map, String player) {
      if (map.get(player) == null)
         return "n/a";

      return nf.format(map.get(player));
   }

   public static void main(String[] args) {
      Server server = new Server(SERVER_PORT);

      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
      context.setContextPath(SERVER_CONTEXT_PATH);
      server.setHandler(context);

      context.addServlet(new ServletHolder(new FoosTrueSkillServer()), SERVER_PATH_SPEC);

      try {
         server.start();
         server.join();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
