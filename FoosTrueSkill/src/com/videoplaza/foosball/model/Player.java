package com.videoplaza.foosball.model;

public class Player {
   private final String name;

   private int rank = 0;

   private double skill = 25d;
   private double uncertainty = 8.333333333333d;

   private Double trueSkill; // only used if explicitly set

   private long goals;
   private long wins;
   private long losses;

   public Player(String name) {
      this.name = name;
   }

   public Player(Player other) {
      name = other.getName();
      skill = other.getSkill();
      uncertainty = other.getUncertainty();
      goals = other.getGoals();
      wins = other.getWins();
      losses = other.getLosses();
   }

   public String getName() {
      return name;
   }

   public double getSkill() {
      return skill;
   }

   public double getTrueSkill() {
      return trueSkill == null ? skill - (3 * uncertainty) : trueSkill;
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

   public void setTrueSkill(double trueSkill) {
      this.trueSkill = trueSkill;
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

   public void win() {
      wins++;
   }

   public void lose() {
      losses++;
   }

   public void score() {
      goals++;
   }

   public int getRank() {
      return rank;
   }

   public void setRank(int rank) {
      this.rank = rank;
   }
}
