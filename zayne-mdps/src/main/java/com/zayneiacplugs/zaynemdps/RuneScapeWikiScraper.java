package com.zayneiacplugs.zaynemdps;

import net.unethicalite.api.utils.MessageUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuneScapeWikiScraper {
    private static final OkHttpClient httpClient = new OkHttpClient();
    private OkHttpClient okHttpClient;

    public static MonsterStats getMonsterStats(String monsterName) {
        String url = "https://oldschool.runescape.wiki/w/" + monsterName.replace(" ", "_");

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String html = response.body().string();
            MonsterStats stats = parseMonsterStats(html);
            return stats;
        } catch (IOException e) {
            System.err.println("Error fetching the page: " + e.getMessage());
            return null;
        }
    }

    private static MonsterStats parseMonsterStats(String html) {
        MonsterStats stats = new MonsterStats();

        // Simple regex pattern to match basic monster stats (very basic and brittle)
        Pattern pattern = Pattern.compile("<tr>\\s*<th>Combat level</th>\\s*<td>(\\d+)</td>\\s*</tr>\\s*" +
                "<tr>\\s*<th>Hitpoints</th>\\s*<td>(\\d+)</td>\\s*</tr>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            stats.setCombatLevel(Integer.parseInt(matcher.group(1)));
            stats.setHitpoints(Integer.parseInt(matcher.group(2)));
        } else {
            System.err.println("Monster stats not found.");
        }

        return stats;
    }
}