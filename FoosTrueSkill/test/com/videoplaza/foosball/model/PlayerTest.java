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

   @Test
   public void testMinus() throws Exception {
      Player p1 = new Player("foo");
      p1.setWins(123);
      p1.setLosses(456);
      p1.setGoals(789);
      p1.setTrueSkill(27d);
      p1.setSkill(2d);
      p1.setUncertainty(3d);

      Player p2 = new Player("bar");
      p2.setWins(112);
      p2.setLosses(445);
      p2.setGoals(778);
      p2.setTrueSkill(16d);
      p2.setSkill(1d);
      p2.setUncertainty(2d);

      Player p3 = p1.minus(p2);

      assertEquals("foo", p3.getName());
      assertEquals(11, p3.getWins());
      assertEquals(11, p3.getLosses());
      assertEquals(11, p3.getGoals());
      assertEquals(11d, p3.getTrueSkill(), 0);
      assertEquals(1d, p3.getSkill(), 0);
      assertEquals(1d, p3.getUncertainty(), 0);
   }

   @Test
   public void minusReturnsCopyForNull() throws Exception {
      Player p1 = new Player("foo");
      p1.setWins(123);
      p1.setLosses(456);
      p1.setGoals(789);
      p1.setTrueSkill(27d);
      p1.setSkill(10d);
      p1.setUncertainty(3d);

      Player p2 = p1.minus(null);

      assertEquals("foo", p2.getName());
      assertEquals(123, p2.getWins());
      assertEquals(456, p2.getLosses());
      assertEquals(789, p2.getGoals());
      assertEquals(1d, p2.getTrueSkill(), 0);
      assertEquals(10d, p2.getSkill(), 0);
      assertEquals(3d, p2.getUncertainty(), 0);
   }

   @Test
   public void testToJson() throws Exception {
      Player p1 = new Player("foo");
      p1.setRank(27);
      p1.setSkill(1.2345);
      p1.setUncertainty(6.7891);
      p1.setWins(123);
      p1.setLosses(456);
      p1.setGoals(789);

      String expected = "{\"rank\":27,\"name\":\"foo\",\"trueskill\":-19.1328,\"mu\":1.2345,\"sigma\":6.7891,\"wins\":123,\"losses\":456,\"goals\":789}";

      assertEquals(expected, p1.toJson());
   }
}
