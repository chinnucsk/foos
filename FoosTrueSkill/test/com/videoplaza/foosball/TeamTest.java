package com.videoplaza.foosball;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
      Player player1 = mock(Player.class);
      when(player1.getTrueSkill()).thenReturn(1.0);

      Player player2 = mock(Player.class);
      when(player2.getTrueSkill()).thenReturn(2.0);

      Team team = new Team(player1, player2);

      assertEquals(1.5, team.getAverageTrueSkill(), 0.0001);
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
