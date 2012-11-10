package com.videoplaza.foosball;

import java.util.Arrays;
import java.util.List;

public class Match {
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

   @Override
   public int hashCode() {
      return 31 * (team1.hashCode() + team2.hashCode());
   }
}
