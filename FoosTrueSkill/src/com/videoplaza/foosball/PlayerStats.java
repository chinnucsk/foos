package com.videoplaza.foosball;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.videoplaza.foosball.model.Player;

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

   public List<Player> getPlayers() {
      return players;
   }

   public String toJson() {
      StringBuilder builder = new StringBuilder();
      builder.append("[\n");

      for (int i = 0; i < players.size(); i++) {
         Player player = players.get(i);
         player.setRank(i + 1);

         builder.append(player.toJson());

         if (i < (players.size() - 1))
            builder.append(",");

         builder.append("\n");
      }

      builder.append("]");

      return builder.toString();
   }
}
