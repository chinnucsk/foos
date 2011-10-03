package se.kth.csc.kattis;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Kattio extends PrintWriter {
   private BufferedReader r;
   private String line;

   private StringTokenizer st;

   private String token;

   public Kattio(InputStream i) {
      super(new BufferedOutputStream(System.out));
      r = new BufferedReader(new InputStreamReader(i));
   }

   public Kattio(InputStream i, OutputStream o) {
      super(new BufferedOutputStream(o));
      r = new BufferedReader(new InputStreamReader(i));
   }

   public double getDouble() {
      return Double.parseDouble(nextToken());
   }

   public int getInt() {
      return Integer.parseInt(nextToken());
   }

   public long getLong() {
      return Long.parseLong(nextToken());
   }

   public String getWord() {
      return nextToken();
   }

   public boolean hasMoreTokens() {
      return peekToken() != null;
   }

   private String nextToken() {
      String ans = peekToken();
      token = null;
      return ans;
   }

   private String peekToken() {
      if (token == null)
         try {
            while (st == null || !st.hasMoreTokens()) {
               line = r.readLine();
               if (line == null)
                  return null;
               st = new StringTokenizer(line);
            }
            token = st.nextToken();
         } catch (IOException e) {
         }
      return token;
   }
}