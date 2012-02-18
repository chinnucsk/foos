package com.videoplaza.foosball;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.videoplaza.foosball.model.Game;

public class FoosTrueSkillServer extends HttpServlet {
   private static final long serialVersionUID = 1L;

   private static final String ONE_DAY_IN_SECONDS = "86400";

   private FoosballDB db = new FoosballDB("rouzbeh.videoplaza.org", "foos");
   private final NumberFormat nf = NumberFormat.getInstance();

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      addCorsHeaders(req, resp);

      Date startDate = FoosballDB.createDate(req.getParameter("startDate"), FoosballDB.DEFAULT_START_DATE);
      Date endDate = FoosballDB.createDate(req.getParameter("endDate"), FoosballDB.DEFAULT_END_DATE);

      String output = db.recalculate(startDate, endDate);

      String path = req.getPathInfo();
      PrintWriter writer = resp.getWriter();
      if (path.endsWith("player")) {
         resp.setContentType("application/json");
         writer.print(output);
      } else if (path.endsWith("game")) {
         SortedMap<Date, Game> games = db.getProcessedGames();
         resp.setContentType("text/plain");
         for (Entry<Date, Game> entry : games.entrySet()) {
            Game game = entry.getValue();

            writer.println(entry.getKey() + "\t" + game.getHomeScore() + "\t" + game.getAwayScore());
            for (String player : game.getHomeTeam()) {
               writer.println(player + "\t" + num(game.getDeltaMu(), player) + "\t" + num(game.getDeltaSigma(), player));
            }

            writer.println("vs");

            for (String player : game.getAwayTeam()) {
               writer.println(player + "\t" + num(game.getDeltaMu(), player) + "\t" + num(game.getDeltaSigma(), player));
            }
            writer.println();
         }
      }
   }

   private void addCorsHeaders(HttpServletRequest req, HttpServletResponse resp) {
      resp.addHeader("Access-Control-Allow-Origin", "*");
      resp.addHeader("Access-Control-Allow-Methods", "DELETE, GET, OPTIONS, POST, PUT");
      String headers = req.getHeader("Access-Control-Request-Headers");
      if (headers != null)
         resp.addHeader("Access-Control-Allow-Headers", headers);
      resp.addHeader("Access-Control-Max-Age", ONE_DAY_IN_SECONDS);
   }

   private String num(Map<String, Double> map, String player) {
      if (map.get(player) == null)
         return "n/a";
      return nf.format(map.get(player));
   }

   public static void main(String[] args) {
      Server server = new Server(8080);

      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
      context.setContextPath("/");
      server.setHandler(context);

      ServletHolder holder = context.addServlet(org.eclipse.jetty.servlet.DefaultServlet.class, "/files/*");
      holder.setInitParameter("resourceBase", "files/BotPoker");
      holder.setInitParameter("pathInfoOnly", "true");

      // Serve some hello world servlets
      context.addServlet(new ServletHolder(new FoosTrueSkillServer()), "/*");
      try {
         server.start();
         server.join();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}