package com.videoplaza.foosball;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.videoplaza.foosball.model.Player;
import org.junit.Test;

import java.util.List;

public class MatchesTest {
   @Test
   public void matchesAreSortedInAscendingTrueSkillDelta() throws Exception {
      Player player1 = mock(Player.class);
      when(player1.getTrueSkill()).thenReturn(1.0);

      Player player2 = mock(Player.class);
      when(player1.getTrueSkill()).thenReturn(2.0);

      Player player3 = mock(Player.class);
      when(player1.getTrueSkill()).thenReturn(3.0);

      Player player4 = mock(Player.class);
      when(player1.getTrueSkill()).thenReturn(4.0);

      Matches matches = new Matches(player1, player2, player3, player4);

      List<Match> matchList = matches.getMatches();

      assertEquals(3, matchList.size());

      double delta1 = matchList.get(0).getTrueSkillDelta();
      double delta2 = matchList.get(1).getTrueSkillDelta();
      double delta3 = matchList.get(2).getTrueSkillDelta();

      assertTrue(delta1 <= delta2);
      assertTrue(delta1 <= delta3);
      assertTrue(delta2 <= delta3);
   }
}
