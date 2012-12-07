package com.videoplaza.foosball;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.videoplaza.foosball.model.Player;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class MatchesTest {
   Matches matches;

   @Before
   public void setup() {
      Player player1 = mock(Player.class);
      when(player1.getTrueSkill()).thenReturn(1.0);
      when(player1.getName()).thenReturn("Player 1");

      Player player2 = mock(Player.class);
      when(player1.getTrueSkill()).thenReturn(2.0);
      when(player1.getName()).thenReturn("Player 2");

      Player player3 = mock(Player.class);
      when(player1.getTrueSkill()).thenReturn(3.0);
      when(player1.getName()).thenReturn("Player 3");

      Player player4 = mock(Player.class);
      when(player1.getTrueSkill()).thenReturn(4.0);
      when(player1.getName()).thenReturn("Player 4");

      matches = new Matches(player1, player2, player3, player4);
   }

   @Test
   public void matchesAreSortedInAscendingTrueSkillDelta() throws Exception {
      List<Match> matchList = matches.getMatches();

      assertEquals(3, matchList.size());

      double delta1 = matchList.get(0).getTrueSkillDelta();
      double delta2 = matchList.get(1).getTrueSkillDelta();
      double delta3 = matchList.get(2).getTrueSkillDelta();

      assertTrue(delta1 <= delta2);
      assertTrue(delta1 <= delta3);
      assertTrue(delta2 <= delta3);
   }

   @Test
   public void testToJson() throws Exception {
      ImmutableList<String> matchJson = FluentIterable.from(matches.getMatches())
         .transform(new Function<Match, String>() {
            @Override
            public String apply(Match match) {
               return match.toJson();
            }
         })
         .toImmutableList();

      String expected = "[" + StringUtils.join(matchJson, ",") + "]";

      assertEquals(expected, matches.toJson());
   }
}
