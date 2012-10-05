package com.videoplaza.foosball.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PlayerTest {
   @Test
   public void trueSkillCalculatedOnlyWhenNotExplicitlySet() {
      Player p1 = new Player("trueSkill unset");
      p1.setSkill(10d);
      p1.setUncertainty(3d);

      assertEquals(1d, p1.getTrueSkill(), 0);

      Player p2 = new Player("trueSkill set explicitly");
      p2.setSkill(10d);
      p2.setUncertainty(3d);
      p2.setTrueSkill(27d);

      assertEquals(27d, p2.getTrueSkill(), 0);
   }

   @Test
   public void testCopyConstructor() throws Exception {
      Player p1 = new Player("foo");
      p1.setSkill(123d);
      p1.setUncertainty(456d);
      p1.setGoals(789);
      p1.setWins(1011);
      p1.setLosses(1213);

      Player p2 = new Player(p1);

      assertEquals("foo", p2.getName());
      assertEquals(123d, p2.getSkill(), 0);
      assertEquals(456d, p2.getUncertainty(), 0);
      assertEquals(789, p2.getGoals());
      assertEquals(1011, p2.getWins());
      assertEquals(1213, p2.getLosses());
   }

   @Test
   public void copyConstructorDoesNotCopyTrueSkill() throws Exception {
      Player p1 = new Player("trueSkill unset");
      p1.setSkill(10d);
      p1.setUncertainty(3d);
      p1.setTrueSkill(27d);

      Player p2 = new Player(p1);

      assertEquals(1d, p2.getTrueSkill(), 0);
   }
}
