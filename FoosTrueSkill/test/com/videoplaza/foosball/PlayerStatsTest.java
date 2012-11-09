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
      String expected = String.format("[%s,%s]", json(player1, 1), json(player2, 2));

      assertEquals(actual, expected);
   }

   @Test
   public void playersWithTooFewGamesSuppressed() throws Exception {
      Player player1 = new Player("player1", 1.0023, 4.0056, 789, 10, 11);
      Player player2 = new Player("player2", 12.1314, 15.1617, 18, 1, 2);
      Player player3 = new Player("player3", 21.2324, 25.2627, 28, 29, 30);

      String actual = new PlayerStats(Arrays.asList(player1, player2, player3), 10).toJson();
      String expected = String.format("[%s,%s]", json(player1, 1), json(player3, 2));

      assertEquals(actual, expected);
   }

   private String json(Player player, int rank) {
      return String.format(
         "{\"rank\":%d,\"name\":\"%s\",\"trueskill\":%1.4f,\"mu\":%1.4f,\"sigma\":%1.4f,\"wins\":%d,\"losses\":%d,\"goals\":%d}\n",
         rank, player.getName(), player.getTrueSkill(), player.getSkill(), player.getUncertainty(), player.getWins(), player.getLosses(), player.getGoals()
      );
   }
}
