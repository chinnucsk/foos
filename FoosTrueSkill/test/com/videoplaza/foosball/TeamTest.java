package com.videoplaza.foosball;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.videoplaza.foosball.model.Player;
import org.junit.Before;
import org.junit.Test;

public class TeamTest {
   private Team team;
   private Team sameOrder;
   private Team differentOrder;
   private Team differentTeam;

   @Before
   public void setup() {
      team = new Team(new Player("player1"), new Player("player2"));
      sameOrder = new Team(new Player("player1"), new Player("player2"));
      differentOrder = new Team(new Player("player2"), new Player("player1"));
      differentTeam = new Team(new Player("player2"), new Player("player3"));
   }

   @Test
   public void testGetAverageTrueSkill() throws Exception {
      Team team = new Team(
         new Player("player1", 26.913, 1.0395, 693, 96, 73),
         new Player("player2", 26.3307, 0.9616, 393, 374, 3489)
      );

      assertEquals(23.6202, team.getAverageTrueSkill(), 0.1);
   }

   @Test
   public void testEquals() throws Exception {
      assertTrue(team.equals(team));
      assertTrue(team.equals(sameOrder));
      assertTrue(team.equals(differentOrder));
      assertFalse(team.equals(differentTeam));
   }

   @Test
   public void equalTeamsHaveSameHashCode() throws Exception {
      assertTrue(team.hashCode() == team.hashCode());
      assertTrue(team.hashCode() == sameOrder.hashCode());
      assertTrue(team.hashCode() == differentOrder.hashCode());
      assertFalse(team.hashCode() == differentTeam.hashCode());
   }
}
