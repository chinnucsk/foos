package com.videoplaza.foosball.model;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

   private final String[] homeTeam;
   private final String[] awayTeam;
   private final Date started;

   private final int homeScore;
   private final int awayScore;

   private final Map<String, Double> deltaMu = new HashMap<String, Double>();
   private final Map<String, Double> deltaSigma = new HashMap<String, Double>();

   public Game(Date date, String home1, String home2, String away1, String away2, int homeScore, int awayScore) {
      started = date;
      this.homeScore = homeScore;
      this.awayScore = awayScore;
      homeTeam = new String[] { home1, home2 };
      awayTeam = new String[] { away1, away2 };
   }

   public int getAwayScore() {
      return awayScore;
   }

   public String[] getAwayTeam() {
      return awayTeam;
   }

   public Map<String, Double> getDeltaMu() {
      return deltaMu;
   }

   public Map<String, Double> getDeltaSigma() {
      return deltaSigma;
   }

   public int getHomeScore() {
      return homeScore;
   }

   public String[] getHomeTeam() {
      return homeTeam;
   }

   public List<Player> getPlayers(Map<String, Player> playerDb) {
      Player[] players = new Player[] { playerDb.get(homeTeam[0]), playerDb.get(homeTeam[1]), playerDb.get(awayTeam[0]), playerDb.get(awayTeam[1]), };
      return Arrays.asList(players);
   }

   public Date getStarted() {
      return started;
   }

}
