package com.videoplaza.foosball.skill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.kth.csc.kattis.Kattio;

import com.videoplaza.foosball.model.Player;

/**
 * Foosball rating tool prototype
 * 
 * Input:
 * an integer n, the number of players
 * a number of lines with 6 integers a1 a2 b1 b2 A B specifying a match 
 * where a[] are the players on team a (a value between 0 and n-1), b[] are the players on team b; 
 * and A and B are the final scores of each team
 * EOF
 * 
 * @author jakob
 * @param <TPlayer>
 *
 */

public class FoosballRating {

   private final List<Player> players = new ArrayList<Player>();

   public FoosballRating(Collection<Player> players) {
      this.players.addAll(players);
   }

   public FoosballRating(int numberOfPlayers) {
      for (int i = 0; i < numberOfPlayers; i++)
         players.add(new Player("Player " + i));
   }

   public void recordMatch(int a1, int a2, int b1, int b2, int scoreA, int scoreB) {
      List<Map<Player, Rating>> teams = new ArrayList<Map<Player, Rating>>();
      {
         Map<Player, Rating> teamA = new HashMap<Player, FoosballRating.Rating>();
         Player player = players.get(a1);
         addPlayerToTeam(teamA, player);
         player = players.get(a2);
         addPlayerToTeam(teamA, player);
         teams.add(teamA);
         Map<Player, Rating> teamB = new HashMap<Player, FoosballRating.Rating>();
         player = players.get(b1);
         addPlayerToTeam(teamB, player);
         player = players.get(b2);
         addPlayerToTeam(teamB, player);
         teams.add(teamB);
      }
      recordMatch(players.get(a1), players.get(a2), players.get(b1), players.get(b2), scoreA, scoreB);
   }

   public void recordMatch(Player home1, Player home2, Player away1, Player away2, int homeScore, int awayScore) {
      List<Map<Player, Rating>> teams = new ArrayList<Map<Player, Rating>>();
      teams.add(makeTeam(home1, home2));
      teams.add(makeTeam(away1, away2));

      boolean homeWon = homeScore == 10;
      Map<Player, Rating> result = calculateNewRatings(new GameInfo(), teams, new int[] { homeWon ? 1 : 2, homeWon ? 2 : 1 });

      saveRatings(result);
   }

   @Override
   public String toString() {
      List<Player> leaderBoard = new ArrayList<Player>();
      leaderBoard.addAll(players);
      Collections.sort(leaderBoard, new Comparator<Player>() {
         @Override
         public int compare(Player o1, Player o2) {
            return o1.getTrueSkill() > o2.getTrueSkill() ? -1 : 1;
         }
      });
      StringBuilder sb = new StringBuilder();
      int rank = 1;
      for (Player player : leaderBoard)
         sb.append(rank++).append(". ").append(player).append("\n");
      return sb.toString();
   }

   protected <TPlayer> Map<TPlayer, Rating> calculateNewRatings(GameInfo gameInfo, List<Map<TPlayer, Rating>> teams, int[] teamRanks) {

      //RankSorter.Sort(teams, teamRanks);
      if (teamRanks[0] > teamRanks[1]) {
         Collections.reverse(teams);
      }

      Map<TPlayer, Rating> team1 = teams.get(0);
      Map<TPlayer, Rating> team2 = teams.get(1);

      boolean wasDraw = (teamRanks[0] == teamRanks[1]);

      Map<TPlayer, Rating> results = new HashMap<TPlayer, Rating>();

      updatePlayerRatings(gameInfo, results, team1, team2, wasDraw ? PairwiseComparison.DRAW : PairwiseComparison.WIN);

      updatePlayerRatings(gameInfo, results, team2, team1, wasDraw ? PairwiseComparison.DRAW : PairwiseComparison.LOSE);

      return results;
   }

   private void addPlayerToTeam(Map<Player, Rating> team, Player player) {
      team.put(player, new Rating(player.getSkill(), player.getUncertainty()));
   }

   private Map<Player, Rating> makeTeam(Player away1, Player away2) {
      Map<Player, Rating> team = new HashMap<Player, FoosballRating.Rating>();
      addPlayerToTeam(team, away1);
      addPlayerToTeam(team, away2);
      return team;
   }

   private void saveRatings(Map<Player, Rating> result) {
      for (Map.Entry<Player, Rating> entry : result.entrySet()) {
         Player player = entry.getKey();
         Rating rating = entry.getValue();
         player.setSkill(rating.getMean());
         player.setUncertainty(rating.getStandardDeviation());
      }
   }

   public static void main(String[] args) {
      Kattio io = new Kattio(System.in, System.out);
      int n = io.getInt();
      FoosballRating foosballRating = new FoosballRating(n);
      while (io.hasMoreTokens()) {
         int a1 = io.getInt();
         int a2 = io.getInt();
         int b1 = io.getInt();
         int b2 = io.getInt();
         int a = io.getInt();
         int b = io.getInt();
         foosballRating.recordMatch(a1, a2, b1, b2, a, b);
         io.print(foosballRating);
         io.println("------------");
         io.flush();
      }
   }

   protected static double square(double value) {
      return value * value;
   }

   private static double getDrawMarginFromDrawProbability(double drawProbability, double beta) {
      return GaussianDistribution.inverseCumulativeTo(.5 * (drawProbability + 1), 0, 1) * Math.sqrt(1 + 1) * beta;
   }

   private static <TPlayer> void updatePlayerRatings(GameInfo gameInfo, Map<TPlayer, Rating> newPlayerRatings, Map<TPlayer, Rating> selfTeam,
                                                     Map<TPlayer, Rating> otherTeam, PairwiseComparison selfToOtherTeamComparison) {
      double drawMargin = getDrawMarginFromDrawProbability(gameInfo.getDrawProbability(), gameInfo.getBeta());
      double betaSquared = square(gameInfo.getBeta());
      double tauSquared = square(gameInfo.getDynamicsFactor());

      int totalPlayers = selfTeam.size() + otherTeam.size();

      //selfTeam.Values.Sum(r => r.Mean);
      double selfMeanSum = 0.0;
      double selfVarianceSum = 0.0;
      for (Rating rating : selfTeam.values()) {
         selfMeanSum += rating.getMean();
         selfVarianceSum += square(rating.getStandardDeviation());
      }

      //double otherTeamMeanSum = otherTeam.Values.Sum(r => r.Mean);
      double otherTeamMeanSum = 0.0;
      double otherTeamVarianceSum = 0.0;
      for (Rating rating : otherTeam.values()) {
         otherTeamMeanSum += rating.getMean();
         otherTeamVarianceSum += square(rating.getStandardDeviation());
      }

      double c = Math.sqrt(selfVarianceSum + otherTeamVarianceSum + totalPlayers * betaSquared);
      double winningMean = selfMeanSum;
      double losingMean = otherTeamMeanSum;

      switch (selfToOtherTeamComparison) {
      case WIN:
      case DRAW:
         // NOP
         break;
      case LOSE:
         winningMean = otherTeamMeanSum;
         losingMean = selfMeanSum;
         break;
      }

      double meanDelta = winningMean - losingMean;

      double v;
      double w;
      double rankMultiplier;

      if (selfToOtherTeamComparison != PairwiseComparison.DRAW) {
         // non-draw case
         v = TruncatedGaussianCorrectionFunctions.vExceedsMargin(meanDelta, drawMargin, c);
         w = TruncatedGaussianCorrectionFunctions.wExceedsMargin(meanDelta, drawMargin, c);
         rankMultiplier = selfToOtherTeamComparison.getValue();
      } else {
         // assume draw
         v = TruncatedGaussianCorrectionFunctions.vWithinMargin(meanDelta, drawMargin, c);
         w = TruncatedGaussianCorrectionFunctions.wWithinMargin(meanDelta, drawMargin, c);
         rankMultiplier = 1;
      }

      for (Map.Entry<TPlayer, Rating> teamPlayerRatingPair : selfTeam.entrySet()) {
         Rating previousPlayerRating = teamPlayerRatingPair.getValue();

         double meanMultiplier = (square(previousPlayerRating.getStandardDeviation()) + tauSquared) / c;
         double stdDevMultiplier = (square(previousPlayerRating.getStandardDeviation()) + tauSquared) / square(c);

         double playerMeanDelta = (rankMultiplier * meanMultiplier * v);
         double newMean = previousPlayerRating.getMean() + playerMeanDelta;

         double newStdDev = Math.sqrt((square(previousPlayerRating.getStandardDeviation()) + tauSquared) * (1 - w * stdDevMultiplier));

         newPlayerRatings.put(teamPlayerRatingPair.getKey(), new Rating(newMean, newStdDev));
      }
   }

   public static class GameInfo {
      public double getBeta() {
         // (sigma_0 / 2) ^2 
         return 8.33333333333333 / 2;
      }

      public double getDrawProbability() {
         return 0;
      }

      public double getDynamicsFactor() {
         return 8.3333333333333 / 100;
      }
   }

   public static enum PairwiseComparison {
      WIN(1), DRAW(0), LOSE(-1);

      private final int value;

      private PairwiseComparison(int value) {
         this.value = value;
      }

      public int getValue() {
         return value;
      }
   }

   public static class Rating {
      private final double mean;
      private final double standardDeviation;

      public Rating(double mean, double stdDev) {
         this.mean = mean;
         standardDeviation = stdDev;
      }

      public double getMean() {
         return mean;
      }

      public double getStandardDeviation() {
         return standardDeviation;
      }
   }

   /// <inheritdoc/>
   /*public <TPlayer> double calculateMatchQuality(GameInfo gameInfo,
                                                         List<Map<TPlayer, Rating>> teams)
   {
       // We've verified that there's just two teams
       ICollection<Rating> team1 = teams.First().Values;
       int team1Count = team1.Count();

       ICollection<Rating> team2 = teams.Last().Values;
       int team2Count = team2.Count();

       int totalPlayers = team1Count + team2Count;

       double betaSquared = Square(gameInfo.Beta);

       double team1MeanSum = team1.Sum(r => r.Mean);
       double team1StdDevSquared = team1.Sum(r => Square(r.StandardDeviation));

       double team2MeanSum = team2.Sum(r => r.Mean);
       double team2SigmaSquared = team2.Sum(r => Square(r.StandardDeviation));

       // This comes from equation 4.1 in the TrueSkill paper on page 8            
       // The equation was broken up into the part under the square root sign and 
       // the exponential part to make the code easier to read.

       double sqrtPart
           = Math.Sqrt(
               (totalPlayers*betaSquared)
               /
               (totalPlayers*betaSquared + team1StdDevSquared + team2SigmaSquared)
               );

       double expPart
           = Math.Exp(
               (-1*Square(team1MeanSum - team2MeanSum))
               /
               (2*(totalPlayers*betaSquared + team1StdDevSquared + team2SigmaSquared))
               );

       return expPart*sqrtPart;
   }*/

}
