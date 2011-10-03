package com.videoplaza.foosball.model;

public class Player {
   private final String name;

   private double skill = 25d;

   private double uncertainty = 8.333333333333d;

   private long goals;
   private long wins;
   private long losses;

   public Player(String name) {
      this.name = name;
   }

   public Player(String name, Double mu, Double sigma) {
      this.name = name;
      if (mu != null)
         skill = mu;
      if (sigma != null)
         uncertainty = sigma;
   }

   public String getName() {
      return name;
   }

   public double getSkill() {
      return skill;
   }

   public double getTrueSkill() {
      return skill - 3 * uncertainty;
   }

   public double getUncertainty() {
      return uncertainty;
   }

   public void setSkill(double skill) {
      this.skill = skill;
   }

   public void setUncertainty(double uncertainty) {
      this.uncertainty = uncertainty;
   }

   @Override
   public String toString() {
      return getName() + " (" + skill + ", " + uncertainty + "): TrueSkill = " + (skill - 3 * uncertainty);
   }

   public void setGoals(long goals) {
      this.goals = goals;
   }

   public long getGoals() {
      return goals;
   }

   public void setWins(long wins) {
      this.wins = wins;
   }

   public long getWins() {
      return wins;
   }

   public void setLosses(long losses) {
      this.losses = losses;
   }

   public long getLosses() {
      return losses;
   }

}
