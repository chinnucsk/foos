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
}
