package com.videoplaza.foosball;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.videoplaza.foosball.model.Player;
import org.junit.Before;
import org.junit.Test;

public class MatchTest {
   private Match match;
   private Match sameMatch;
   private Match differentOrder;
   private Match differentMatch;

   @Before
   public void setup() {
      Team team1 = new Team(new Player("player1"), new Player("player2"));
      Team team2 = new Team(new Player("player1"), new Player("player3"));
      Team team3 = new Team(new Player("player1"), new Player("player4"));

      match = new Match(team1, team2);
      sameMatch = new Match(team1, team2);
      differentOrder = new Match(team2, team1);
      differentMatch = new Match(team1, team3);
   }

   @Test
   public void testTrueSkillDelta() throws Exception {
      Team team1 = mock(Team.class);
      when(team1.getAverageTrueSkill()).thenReturn(1.2345);

      Team team2 = mock(Team.class);
      when(team2.getAverageTrueSkill()).thenReturn(2.3456);

      Team team3 = mock(Team.class);
      when(team3.getAverageTrueSkill()).thenReturn(2.3456);

      assertEquals(1.1111, new Match(team1, team2).getTrueSkillDelta(), 0.00001);
      assertEquals(1.1111, new Match(team2, team1).getTrueSkillDelta(), 0.00001);
      assertEquals(0.0, new Match(team2, team2).getTrueSkillDelta(), 0.00001);
      assertEquals(0.0, new Match(team2, team3).getTrueSkillDelta(), 0.00001);
   }

   @Test
   public void testEquals() throws Exception {
      assertTrue(match.equals(match));
      assertTrue(match.equals(sameMatch));
      assertTrue(match.equals(differentOrder));
      assertFalse(match.equals(differentMatch));
   }

   @Test
   public void equalTeamsHaveSameHashCode() throws Exception {
      assertTrue(match.hashCode() == match.hashCode());
      assertTrue(match.hashCode() == sameMatch.hashCode());
      assertTrue(match.hashCode() == differentOrder.hashCode());
      assertFalse(match.hashCode() == differentMatch.hashCode());
   }
}
