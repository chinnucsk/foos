package com.videoplaza.foosball;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.videoplaza.foosball.model.Player;

import java.text.NumberFormat;
import java.util.List;

public class PlayerStats {
   private final List<Player> players;

   public PlayerStats(List<Player> players) {
      this(players, 0);
   }

   public PlayerStats(List<Player> players, final int minRequiredGames) {
      this.players = FluentIterable.from(players)
         .filter(new Predicate<Player>() {
            @Override
            public boolean apply(Player player) {
               return (player.getWins() + player.getLosses()) >= minRequiredGames;
            }
         })
      .toImmutableList();
   }

   public String toJson() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");

      int rank = 1;
      boolean first = true;
      for (Player player : players) {
         player.setRank(rank++);

         if (!first)
            sb.append(",");
         appendPlayerJson(sb, player);

         first = false;
      }

      sb.append("]");

      return sb.toString();
   }

   private void appendPlayerJson(StringBuilder sb, Player player) {
      sb.append("{");
      json(sb, "rank", player.getRank()).append(",");
      json(sb, "name", player.getName()).append(",");
      json(sb, "trueskill", player.getTrueSkill()).append(",");
      json(sb, "mu", player.getSkill()).append(",");
      json(sb, "sigma", player.getUncertainty()).append(",");
      json(sb, "wins", player.getWins()).append(",");
      json(sb, "losses", player.getLosses()).append(",");
      json(sb, "goals", player.getGoals());
      sb.append("}\n");
   }

   private StringBuilder json(StringBuilder sb, String name, Number value) {
      NumberFormat nf = NumberFormat.getInstance();
      nf.setGroupingUsed(false);
      nf.setMaximumFractionDigits(4);

      return sb.append("\"").append(name).append("\"").append(":").append(value != null ? nf.format(value) : 0);
   }

   private StringBuilder json(StringBuilder sb, String name, String value) {
      return sb.append("\"").append(name).append("\"").append(":\"").append(value).append('"');
   }
}
