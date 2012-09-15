import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.MathTool;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

   private static File OUTPUT_DIR;


   private static final Map<String, int[]> POSITION_MAP = new HashMap<String, int[]>() {{
      // goalkeeper
      put("w0", new int[] { 0, 2 }); // w0
      // d-line
      put("w1", new int[] { 1, 3 }); // w1
      put("w2", new int[] { 1, 1 }); // w2
      // midfield
      put("w3", new int[] { 3, 4 }); // w3
      put("w4", new int[] { 3, 3 }); // w4
      put("w5", new int[] { 3, 2 }); // w5
      put("w6", new int[] { 3, 1 }); // w6
      put("w7", new int[] { 3, 0 }); // w7
      // o-line
      put("w8", new int[] { 5, 3 }); // w8
      put("w9", new int[] { 5, 2 }); // w9
      put("w10", new int[] { 5, 1 });  // w10

      put("b0", new int[] { 7, 2 }); // b0
      // d-line
      put("b1", new int[] { 6, 1 }); // b1
      put("b2", new int[] { 6, 3 }); // b2
      // midfield
      put("b3", new int[] { 4, 0 }); // b3
      put("b4", new int[] { 4, 1 }); // b4
      put("b5", new int[] { 4, 2 }); // b5
      put("b6", new int[] { 4, 3 }); // b6
      put("b7", new int[] { 4, 4 }); // b7
      // o-line
      put("b8", new int[] { 2, 1 }); // b8
      put("b9", new int[] { 2, 2 }); // b9
      put("b10", new int[] { 2, 3 });  // b10
   }};


   public static void main(String[] args) {

      if (args.length < 1) {
         System.err.println("Error: invalid number of arguments.");
         System.err.println("Usage: ");
         System.err.println("       java -jar heatmap-generator.jar <output dir>");
         System.err.println("Example: ");
         System.err.println("       java -jar heatmap-generator.jar /var/www/heatmaps/");
         System.exit(1);
      }

      OUTPUT_DIR = new File(args[0]);

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
      generateHtml(heatMaps);
      System.out.println("Done");
   }

   private static Map<String, Map<String, Integer>> prepareHeatMaps() {
      System.out.println("Downloading data from server");
      Map<String, Map<String, Integer>> heatMaps = new HashMap<String, Map<String, Integer>>();
      Mongo m = null;
      try {
         m = new Mongo(MONGO_HOST, MONGO_PORT);
      } catch (UnknownHostException e) {
         e.printStackTrace();
         return heatMaps;
      }
      DB db = m.getDB(MONGO_DB);

      List<String> playerNames = new ArrayList<String>();
      DBCollection coll = db.getCollection("player");
      DBCursor cursor = coll.find();
      try {
         while (cursor.hasNext()) {
            DBObject o = cursor.next();
            JSONObject doc = (JSONObject) JSONValue.parse(o.toString());
            if (!doc.containsKey("name"))
               continue;
            playerNames.add(doc.get("name").toString());
         }
      } finally {
         cursor.close();
      }

      System.out.println("Found " + playerNames.size() + " players");

      coll = db.getCollection("game");
      cursor = coll.find();


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

   private static Map<String, Integer> getMaxValues(Map<String, Map<String, Integer>> heatMaps) {
      Map<String, Integer> ret = new HashMap<String, Integer>();
      for (String player: heatMaps.keySet()) {
         ret.put(player, Collections.max(heatMaps.get(player).values()));
      }
      return ret;
   }

   private static void generateHtml(Map<String, Map<String, Integer>> heatMaps) {
      System.out.println("Generating HTML index page");
      VelocityEngine engine = new VelocityEngine();
      engine.setProperty("resource.loader", "classpath");
      engine.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
      engine.init();
      //Velocity.init();
      VelocityContext context = new VelocityContext();
      List<String> playerNames = new LinkedList<String>(heatMaps.keySet());
      Collections.sort(playerNames);
      context.put("players", playerNames);
      context.put("heatmaps", heatMaps);
      context.put("maxvalues", getMaxValues(heatMaps));
      context.put("positions", POSITION_MAP);
      context.put("math", new MathTool());

      try {
         Template template = engine.getTemplate("index.vm"); // Velocity.getTemplate("index.vm");
         StringWriter sw = new StringWriter();
         template.merge(context, sw);
         FileUtils.writeStringToFile(new File(OUTPUT_DIR, "index.html"), sw.toString(), "UTF-8");
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
