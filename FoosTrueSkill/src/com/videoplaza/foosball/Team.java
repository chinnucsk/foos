package com.videoplaza.foosball;

import com.videoplaza.foosball.model.Player;

import java.util.List;

public class Team {
   private final List<Player> players;

   public Team(List<Player> players) {
      if (players.size() != 2)
         throw new IllegalArgumentException("a team must contain exactly 2 players!");

      this.players = players;
   }

   public double getAverageTrueSkill() {
      return (players.get(0).getTrueSkill() + players.get(1).getTrueSkill()) / 2.0;
   }

   @Override
   public boolean equals(Object other) {
      return other instanceof Team && this.hashCode() == other.hashCode();
   }

   @Override
   public int hashCode() {
      return 31 * (players.get(0).getName().hashCode() + players.get(1).getName().hashCode());
   }
}
