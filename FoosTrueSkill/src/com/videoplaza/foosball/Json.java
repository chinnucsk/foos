package com.videoplaza.foosball;

import java.text.NumberFormat;

public class Json {
   public static StringBuilder json(StringBuilder sb, String name, Number value) {
      NumberFormat nf = NumberFormat.getInstance();
      nf.setGroupingUsed(false);
      nf.setMaximumFractionDigits(4);

      return sb.append("\"").append(name).append("\"").append(":").append(value != null ? nf.format(value) : 0);
   }

   public static StringBuilder json(StringBuilder sb, String name, String value) {
      return sb.append("\"").append(name).append("\"").append(":\"").append(value).append('"');
   }
}
