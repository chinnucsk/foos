package com.videoplaza.foosball;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class Cors {
   private static final String ONE_DAY_IN_SECONDS = "86400";
   private static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
   private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

   private static final Map<String, String> DEFAULT_HEADERS = new HashMap<String, String>();

   static {
      DEFAULT_HEADERS.put("Access-Control-Allow-Origin", "*");
      DEFAULT_HEADERS.put("Access-Control-Allow-Methods", "DELETE, GET, OPTIONS, POST, PUT");
      DEFAULT_HEADERS.put("Access-Control-Max-Age", ONE_DAY_IN_SECONDS);
   }

   public static void addHeaders(HttpServletRequest req, HttpServletResponse resp) {
      for (Map.Entry<String, String> entry : DEFAULT_HEADERS.entrySet())
         resp.addHeader(entry.getKey(), entry.getValue());

      String accessControlRequestHeaders = req.getHeader(ACCESS_CONTROL_REQUEST_HEADERS);
      if (accessControlRequestHeaders != null)
         resp.addHeader(ACCESS_CONTROL_ALLOW_HEADERS, accessControlRequestHeaders);
   }
}
