package com.videoplaza.foosball;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
