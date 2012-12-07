package com.videoplaza.foosball;

import static com.videoplaza.foosball.Json.json;

import java.util.Arrays;
import java.util.List;

public class Match implements Comparable<Match> {
   private final Team team1;
   private final Team team2;

   public Match(Team team1, Team team2) {
      this.team1 = team1;
      this.team2 = team2;
   }

   public List<Team> getTeams() {
      return Arrays.asList(team1, team2);
   }

   public double getTrueSkillDelta() {
      return Math.abs(team1.getAverageTrueSkill() - team2.getAverageTrueSkill());
   }

   @Override
   public boolean equals(Object other) {
      return other instanceof Match && this.hashCode() == other.hashCode();
   }

   public String toJson() {
      StringBuilder builder = new StringBuilder();
      builder.append("{\"teams\":[");
      builder.append(team1.toJson());
      builder.append(",");
      builder.append(team2.toJson());
      builder.append("],");
      json(builder, "trueSkillDelta", getTrueSkillDelta());
      builder.append("}");

      return builder.toString();
   }

   @Override
   public int hashCode() {
      return 31 * (team1.hashCode() + team2.hashCode());
   }

   @Override
   public int compareTo(Match other) {
      if (this.getTrueSkillDelta() < other.getTrueSkillDelta())
         return -1;

      if (this.getTrueSkillDelta() > other.getTrueSkillDelta())
         return 1;

      return 0;
   }
}
