package com.videoplaza.foosball;

import static com.videoplaza.foosball.Json.json;

import com.videoplaza.foosball.model.Player;

public class Team {
   private final Player player1;
   private final Player player2;

   public Team(Player player1, Player player2) {
      this.player1 = player1;
      this.player2 = player2;
   }

   public double getAverageTrueSkill() {
      return (player1.getTrueSkill() + player2.getTrueSkill()) / 2.0;
   }

   public String toJson() {
      StringBuilder builder = new StringBuilder();
      builder.append("{\"players\":[\"");
      builder.append(player1.getName());
      builder.append("\",");
      builder.append("\"");
      builder.append(player2.getName());
      builder.append("\"],");
      json(builder, "averageTrueSkill", getAverageTrueSkill());
      builder.append("}");

      return builder.toString();
   }

   @Override
   public boolean equals(Object other) {
      return other instanceof Team && this.hashCode() == other.hashCode();
   }

   @Override
   public int hashCode() {
      return 31 * (player1.getName().hashCode() + player2.getName().hashCode());
   }
}
