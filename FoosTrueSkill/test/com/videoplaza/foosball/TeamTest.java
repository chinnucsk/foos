package com.videoplaza.foosball;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.videoplaza.foosball.model.Player;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeamTest {
   @Test
   public void constructorThrowsExceptionIfNotGivenExactlyTwoPlayers() throws Exception {
      catchExceptionOrFail(new ArrayList<Player>());
      catchExceptionOrFail(Arrays.asList(new Player("player1")));
      catchExceptionOrFail(Arrays.asList(new Player("player1"), new Player("player2"), new Player("player3")));

      try {
         new Team(Arrays.asList(new Player("player1"), new Player("player2")));
      } catch (IllegalArgumentException e) {
         fail("constructor threw unexpected exception");
      }
   }

   @Test
   public void testGetAverageTrueSkill() throws Exception {
      Team team = new Team(Arrays.asList(
         new Player("player1", 26.913, 1.0395, 693, 96, 73),
         new Player("player2", 26.3307, 0.9616, 393, 374, 3489)
      ));

      assertEquals(23.6202, team.getAverageTrueSkill(), 0.1);
   }

   private void catchExceptionOrFail(List<Player> players) {
      try {
         new Team(players);
      } catch (IllegalArgumentException e) {
         return;
      }

      fail("constructor with empty list did not throw exception");
   }
}
