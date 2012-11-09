package com.videoplaza.foosball;

import javax.servlet.http.HttpServletRequest;

public enum RequestType {
   GAME("game"),
   MATCHMAKER("matchmaker"),
   PLAYER("player");

   private final String path;

   RequestType(String path) {
      this.path = path;
   }

   public String getPath() {
      return path;
   }

   static RequestType from(HttpServletRequest request) {
      String path = request.getPathInfo();

      for (RequestType type : values())
         if (path.endsWith(type.getPath()))
            return type;

      throw new IllegalArgumentException("could not determine request type from path '" + path +"'");
   }
}
