package com.videoplaza.foosball;

import com.videoplaza.foosball.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Matches {
   private final List<Match> matches;

   public Matches(Player player1, Player player2, Player player3, Player player4) {
      matches = permute(player1, player2, player3, player4);
      Collections.sort(matches);
   }

   public List<Match> getMatches() {
      return matches;
   }

   public String toJson() {
      return null;
   }

   private List<Match> permute(Player player1, Player player2, Player player3, Player player4) {
      List<Match> matchList = new ArrayList<Match>();
      matchList.add(new Match(new Team(player1, player2), new Team(player3, player4)));
      matchList.add(new Match(new Team(player1, player3), new Team(player2, player4)));
      matchList.add(new Match(new Team(player1, player4), new Team(player2, player3)));

      return matchList;
   }
}
