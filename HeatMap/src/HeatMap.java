import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: krisztian
 * Date: 8/25/12
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class HeatMap {
   private static final String MONGO_HOST = "rouzbeh.videoplaza.org";
   private static final int MONGO_PORT = 27017;
   private static final String MONGO_DB = "foos";
   private static final String NO_DATA_PLACEHOLDER = "? ";
   private static final int BG_VALUE = -1;

   private static File GNUPLOT;
   private static File OUTPUT_DIR;

   private static final int[][] WHITE_POSITIONS = new int[][] {
      // goalkeeper
      { 0, 2 }, // w0
      // d-line
      { 1, 3 }, // w1
      { 1, 1 }, // w2
      // midfield
      { 3, 4 }, // w3
      { 3, 3 }, // w4
      { 3, 2 }, // w5
      { 3, 1 }, // w6
      { 3, 0 }, // w7
      // o-line
      { 5, 3 }, // w8
      { 5, 2 }, // w9
      { 5, 1 }  // w10
   };

   private static final int[][] BLACK_POSITIONS = new int[][] {
      // goalkeeper
      { 7, 2 }, // b0
      // d-line
      { 6, 1 }, // b1
      { 6, 3 }, // b2
      // midfield
      { 4, 0 }, // b3
      { 4, 1 }, // b4
      { 4, 2 }, // b5
      { 4, 3 }, // b6
      { 4, 4 }, // b7
      // o-line
      { 2, 1 }, // b8
      { 2, 2 }, // b9
      { 2, 3 }  // b10
   };

   public static void main(String[] args) {

      if (args.length < 2) {
         System.err.println("Error: invalid number of arguments.");
         System.err.println("Usage: ");
         System.err.println("       java -jar heatmap-generator.jar <gnuplot_path> <output dir>");
         System.err.println("Example: ");
         System.err.println("       java -jar heatmap-generator.jar /usr/local/bin/gnuplot /var/www/heatmaps/");
         System.exit(1);
      }

      GNUPLOT = new File(args[0]);
      OUTPUT_DIR = new File(args[1]);

      if (OUTPUT_DIR.exists()) {
         if (OUTPUT_DIR.isDirectory()) {
            System.out.println("Using existing dir " + OUTPUT_DIR.getAbsolutePath());
         } else {
            System.err.println("Output directory is not a directory, but a file");
            System.exit(1);
         }
      } else {
         if (OUTPUT_DIR.mkdirs()) {
            System.out.println("Output dir " + OUTPUT_DIR.getAbsolutePath() + " created");
         } else {
            System.err.println("Cannot create output directory " + OUTPUT_DIR.getAbsolutePath());
            System.exit(1);
         }
      }

      Map<String, Map<String, Integer>> heatMaps = prepareHeatMaps();
      generateImages(heatMaps);
      generateIndexPage(heatMaps);
      System.out.println("Done");
   }

   private static Map<String, Map<String, Integer>> prepareHeatMaps() {
      System.out.println("Downloading data from server");
      Mongo m = null;
      try {
         m = new Mongo(MONGO_HOST, MONGO_PORT);
      } catch (UnknownHostException e) {
         e.printStackTrace();
      }
      DB db = m.getDB(MONGO_DB);

      List<String> playerNames = new ArrayList<String>();
      DBCollection coll = db.getCollection("player");
      DBCursor cursor = coll.find();
      try {
         while (cursor.hasNext()) {
            DBObject o = cursor.next();
            JSONObject doc = (JSONObject) JSONValue.parse(o.toString());
            playerNames.add(doc.get("name").toString());
         }
      } finally {
         cursor.close();
      }

      System.out.println("Found " + playerNames.size() + " players");

      coll = db.getCollection("game");
      cursor = coll.find();

      Map<String, Map<String, Integer>> heatMaps = new HashMap<String, Map<String, Integer>>();

      for (String player : playerNames) {
         HashMap<String, Integer> playerHM = new HashMap<String, Integer>();
         for (int i = 0; i <= 10; i++) {
            playerHM.put("b" + i, 0);
            playerHM.put("w" + i, 0);
            playerHM.put("o" + i, 0);
            heatMaps.put(player, playerHM);
         }
      }

      heatMaps.put("control_player", new HashMap<String, Integer>() {{
         for (int i = 0; i <= 10; i++) {
            put("b" + i, (i + 1) * 10);
            put("w" + i, (i + 1) * 10);
            put("o" + i, (i + 1) * 10);
         }
      }});

      try {
         while (cursor.hasNext()) {
            DBObject o = cursor.next();
            JSONObject doc = (JSONObject) JSONValue.parse(o.toString());
            JSONArray scores = (JSONArray) doc.get("scores");
            if (scores == null)
               // skip ongoing games
               continue;

            //System.out.println(scores);
            for (int i = 0; i < scores.size(); i++) {
               JSONObject score = (JSONObject) scores.get(i);
               // System.out.println(score);

               String player = score.get("player").toString();
               String pos = score.get("position").toString();
               if (player.trim().isEmpty())
                  continue;
               if (pos.trim().isEmpty())
                  continue;

               if (score.containsKey("ownGoal") && score.get("ownGoal").equals(Boolean.TRUE)) {
                  pos = "o" + pos.substring(1);
               }

               // System.out.println(player + " -> " + pos );
               Map<String, Integer> playerHM = heatMaps.get(player);
               Integer value = playerHM.get(pos);
               if (value == null) {
                  System.out.println("Unknown position: " + pos);
                  value = 0;
               }
               value++;
               playerHM.put(pos, value);
            }
         }
      } finally {
         cursor.close();
      }
      return heatMaps;

   }

   private static void generateImages(Map<String, Map<String, Integer>> heatMaps) {
      System.out.println("Generating heat map images for " + heatMaps.size() + " players");
      for (String player : heatMaps.keySet()) {
         String white = generateWhiteHeatMapMatrix(player, heatMaps);
         String black = generateBlackHeatMapMatrix(player, heatMaps);
         String ownGoal = generateOwnGoalHeatMapMatrix(player, heatMaps);

         int whiteMax = getMaxValue(heatMaps.get(player), "w");
         int blackMax = getMaxValue(heatMaps.get(player), "b");
         int owngoalMax = getMaxValue(heatMaps.get(player), "o");

         generateImage(player + " as white player", player + "_white.png", white, whiteMax);
         generateImage(player + " as black player", player + "_black.png", black, blackMax);
         generateImage(player + " own goals", player + "_owngoal.png", ownGoal, owngoalMax);
      }
   }

   private static int getMaxValue(Map<String, Integer> heatMap, String playerPrefix) {
      int max = 0;
      for (String position : heatMap.keySet()) {
         if (position.startsWith(playerPrefix)) {
            max = Math.max(max, heatMap.get(position));
         }
      }
      return max;
   }

   private static void generateIndexPage(Map<String, Map<String, Integer>> heatMaps) {
      System.out.println("Generating index page");
      StringBuilder html = new StringBuilder("<!DOCTYPE HTML>\n");
      html.append("<html>\n");
      html.append("<head>\n");
      html.append("<meta charset=\"UTF-8\" /></meta>\n");
      html.append("<title>Foos Heat Maps</title>\n");
      html.append("</head>\n");

      html.append("<body>\n");
      html.append("<h1 id='top'>Foos Heat Maps</h1>\n");

      List<String> playerNames = new ArrayList<String>(heatMaps.keySet());
      Collections.sort(playerNames);
      for (String player : playerNames) {
         html.append("<a href='#" + player + "'>" + player + "</a>\n");
      }

      for (String player : playerNames) {

         html.append("<h2 id='" + player + "'>");
         html.append(player);
         html.append("&nbsp;<small><a href='#top'>Jump to top</a> </h2></small>\n");

         html.append("<img width='33%' src='" + player + "_white.png' />");
         html.append("<img width='33%' src='" + player + "_black.png' />");
         html.append("<img width='33%' src='" + player + "_owngoal.png' />");
      }
      html.append("</body></html>");
      BufferedWriter bw = null;
      try {
         bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(OUTPUT_DIR, "index.html")), "UTF-8"));
         bw.write(html.toString());
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (bw != null)
            try {
               bw.close();
            } catch (IOException e) {
               e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
      }
   }

   private static String generateWhiteHeatMapMatrix(String player, Map<String, Map<String, Integer>> heatMaps) {
      return generateHeatMapMatrix(player, heatMaps, 'w', WHITE_POSITIONS);
   }

   private static String generateBlackHeatMapMatrix(String player, Map<String, Map<String, Integer>> heatMaps) {
      return generateHeatMapMatrix(player, heatMaps, 'b', BLACK_POSITIONS);
   }

   private static String generateOwnGoalHeatMapMatrix(String player, Map<String, Map<String, Integer>> heatMaps) {
      return generateHeatMapMatrix(player, heatMaps, 'o', WHITE_POSITIONS);
   }

   private static void saveHeatMapMatrix(String player, Map<String, Map<String, Integer>> heatMaps, char type, int[][] positions) {
      try {
         File outfile = new File(OUTPUT_DIR, player + "_" + type + ".hm");
         BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
         bw.write(generateHeatMapMatrix(player, heatMaps, type, positions));
         bw.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   private static String generateHeatMapMatrix(String player, Map<String, Map<String, Integer>> heatMaps, char type, int[][] positions) {
      StringBuilder b = new StringBuilder();
      try {
         Map<String, Integer> playerHM = heatMaps.get(player);
         int[][] matrix = new int[8][5];
         for (int j = 0; j <= 4; j++)
            // Set 'background' value to a special value to make
            // it distinguishable from the zero goal players
            for (int i = 0; i <= 7; i++)
               matrix[i][j] = BG_VALUE;

         for (int i = 0; i <= 10; i++) {
            int x = positions[i][0];
            int y = positions[i][1];
            int value = playerHM.get(type + "" + i);
            matrix[x][y] = value;
         }

         for (int j = 0; j <= 4; j++) {
            for (int i = 0; i <= 7; i++) {
               if (matrix[i][j] == BG_VALUE) {
                  b.append(NO_DATA_PLACEHOLDER);
               } else {
                  b.append(matrix[i][j] + " ");
               }
               b.append(NO_DATA_PLACEHOLDER); // Add placeholder values to pull the player apart
            }
            b.append("\n");

            // Add placeholder row to pull the player apart
            for (int i = 0; i <= 15; i++)
               b.append(NO_DATA_PLACEHOLDER);
            b.append("\n");
         }

      } catch (Exception e) {
         e.printStackTrace();
      }
      return b.toString();
   }

   private static void generateImage(String title, String filename, String matrix, int maxValue) {
      StringBuilder plotScript = new StringBuilder();
      plotScript.append("set title '");
      plotScript.append(title);
      plotScript.append("'\n");
      plotScript.append("set datafile missing \"?\"\n");
      plotScript.append("set cbrange ["+BG_VALUE+":" + Math.max(1,maxValue) + "]\n");
      plotScript.append("set palette defined (0 'blue', 1 'red')\n");
      plotScript.append("unset xtics\n");
      plotScript.append("unset ytics\n");
      plotScript.append("set yrange [-1:9]\n");
      plotScript.append("set xrange [-1:15]\n");
      plotScript.append("set terminal png\n");
      plotScript.append("set output '");
      plotScript.append(new File(OUTPUT_DIR, filename).getAbsolutePath());
      plotScript.append("'\n");

      plotScript.append("plot '-' matrix with image\n");
      plotScript.append(matrix);
      plotScript.append("e\n");
      // System.out.print("Generating " + filename);
      try {
         File tmpFile = File.createTempFile("heatmap", ".gnuplot");
         BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile), "UTF-8"));
         bw.write(plotScript.toString());
         bw.close();

         // System.out.println(plotScript.toString());
         // Process p = Runtime.getRuntime().exec("/bin/ls", new String[] { tmpFile.getAbsolutePath() });
         Process p = Runtime.getRuntime().exec(GNUPLOT.getAbsolutePath() + " " + tmpFile.getAbsolutePath());
         p.waitFor();
         tmpFile.delete();
         if (p.exitValue() != 0) {
            System.out.println("ERROR: could not generate heatmap '" + title + "'. Gnuplot exited with " + p.exitValue());
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            System.out.println("Gnuplot error output: ");
            String line = null;
            while ((line = br.readLine()) != null)
               System.out.println(line);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

}
