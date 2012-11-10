package com.videoplaza.foosball;

import static org.junit.Assert.assertEquals;

import com.videoplaza.foosball.model.Player;
import org.junit.Test;

import java.util.Arrays;

public class PlayerStatsTest {
   @Test
   public void testToJson() throws Exception {
      Player player1 = new Player("player1", 1.0023, 4.0056, 789, 10, 11);
      Player player2 = new Player("player2", 12.1314, 15.1617, 18, 19, 20);

      String actual = new PlayerStats(Arrays.asList(player1, player2)).toJson();
      String expected = String.format("[\n%s,\n%s\n]", player1.toJson(), player2.toJson());

      assertEquals(actual, expected);
   }

   @Test
   public void playersWithTooFewGamesSuppressed() throws Exception {
      Player player1 = new Player("player1", 1.0023, 4.0056, 789, 10, 11);
      Player player2 = new Player("player2", 12.1314, 15.1617, 18, 1, 2);
      Player player3 = new Player("player3", 21.2324, 25.2627, 28, 29, 30);

      String actual = new PlayerStats(Arrays.asList(player1, player2, player3), 10).toJson();
      String expected = String.format("[\n%s,\n%s\n]", player1.toJson(), player3.toJson());

      assertEquals(actual, expected);
   }
}
