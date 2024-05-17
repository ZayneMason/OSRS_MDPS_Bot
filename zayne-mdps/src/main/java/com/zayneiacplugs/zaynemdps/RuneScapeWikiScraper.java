package com.zayneiacplugs.zaynemdps;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class RuneScapeWikiScraper {
    private static final String BASE_URL = "https://oldschool.runescape.wiki/w/Special:Ask?class=sortable+wikitable+smwtable&format=json&headers=show&limit=500&link=all&mainlabel=&offset=0&order=asc&prefix=none&prettyprint=true&searchlabel=JSON&sort=&unescape=true&x=-5B-5BCategory%3AMonsters-5D-5D-20-5B-5BName%3A%3A";
    private static final String URL_SUFFIX = "%5D%5D%2F-3FAll-20NPC-20ID%2F-3FCombat-20level%2F-3FHitpoints%2F-3FAll-20Attack-20style%2F-3FAttack-20speed%2F-3FDefence-20level%2F-3FStab-20defence-20bonus%2F-3FSlash-20defence-20bonus%2F-3FCrush-20defence-20bonus%2F-3FMagic-20level%2F-3FMagic-20defence-20bonus%2F-3FRange-20defence-20bonus";

    public static MonsterStats getMonsterStats(String npcName) throws IOException {
        if (npcName.equalsIgnoreCase("Minotaur")) {
            return getMinotaurStats();
        }

        String encodedNpcName = URLEncoder.encode(npcName, StandardCharsets.UTF_8.toString());
        String urlString = BASE_URL + encodedNpcName + URL_SUFFIX;
        System.out.println("Fetching data from URL: " + urlString); // Log the URL
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == 404) {
            throw new RuntimeException("HTTP 404 Not Found: " + urlString);
        } else if (responseCode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responseCode);
        }

        Scanner scanner = new Scanner(url.openStream());
        StringBuilder inline = new StringBuilder();
        while (scanner.hasNext()) {
            inline.append(scanner.nextLine());
        }
        scanner.close();

        String response = inline.toString();

        // Log the response for debugging
        System.out.println("Response: " + response);

        // Ensure the response is not empty and is valid JSON
        if (response.isEmpty()) {
            throw new RuntimeException("Empty response received from URL: " + urlString);
        }
        if (!response.trim().startsWith("{")) {
            throw new RuntimeException("Unexpected response format: " + response);
        }

        // Parse the JSON response
        JSONObject jsonResponse = new JSONObject(response);
        JSONObject results = jsonResponse.optJSONObject("results");
        if (results == null || !results.has(npcName)) {
            throw new RuntimeException("No data found for NPC: " + npcName);
        }

        JSONObject npcData = results.getJSONObject(npcName).optJSONObject("printouts");
        if (npcData == null) {
            throw new RuntimeException("No printouts data found for NPC: " + npcName);
        }

        MonsterStats monsterStats = new MonsterStats();
        monsterStats.setNpcId(getIntFromJSONArray(npcData, "All NPC ID"));
        monsterStats.setCombatLevel(getIntFromJSONArray(npcData, "Combat level"));
        monsterStats.setHitpoints(getIntFromJSONArray(npcData, "Hitpoints"));
        monsterStats.setAttackStyles(getStringFromJSONArray(npcData, "All Attack style"));
        monsterStats.setAttackSpeed(getIntFromJSONArray(npcData, "Attack speed"));
        monsterStats.setDefenceLevel(getIntFromJSONArray(npcData, "Defence level"));
        monsterStats.setStabDefenceBonus(getIntFromJSONArray(npcData, "Stab defence bonus"));
        monsterStats.setSlashDefenceBonus(getIntFromJSONArray(npcData, "Slash defence bonus"));
        monsterStats.setCrushDefenceBonus(getIntFromJSONArray(npcData, "Crush defence bonus"));
        monsterStats.setMagicLevel(getIntFromJSONArray(npcData, "Magic level"));
        monsterStats.setMagicDefenceBonus(getIntFromJSONArray(npcData, "Magic defence bonus"));
        monsterStats.setRangeDefenceBonus(getIntFromJSONArray(npcData, "Range defence bonus"));

        return monsterStats;
    }

    private static MonsterStats getMinotaurStats() {
        MonsterStats minotaurStats = new MonsterStats();
        minotaurStats.setNpcId(0); // As it's not provided
        minotaurStats.setCombatLevel(318);
        minotaurStats.setHitpoints(225);
        minotaurStats.setAttackStyles("Melee");
        minotaurStats.setAttackSpeed(5);
        minotaurStats.setDefenceLevel(190);
        minotaurStats.setStabDefenceBonus(0);
        minotaurStats.setSlashDefenceBonus(0);
        minotaurStats.setCrushDefenceBonus(0);
        minotaurStats.setMagicLevel(250);
        minotaurStats.setMagicDefenceBonus(0);
        minotaurStats.setRangeDefenceBonus(12);
        return minotaurStats;
    }

    private static int getIntFromJSONArray(JSONObject jsonObject, String key) {
        JSONArray jsonArray = jsonObject.optJSONArray(key);
        if (jsonArray != null && jsonArray.length() > 0) {
            return jsonArray.optInt(0, 0);
        }
        return 0; // Default value if not found
    }

    private static String getStringFromJSONArray(JSONObject jsonObject, String key) {
        JSONArray jsonArray = jsonObject.optJSONArray(key);
        if (jsonArray != null && jsonArray.length() > 0) {
            return jsonArray.toString();
        }
        return ""; // Default value if not found
    }
}
