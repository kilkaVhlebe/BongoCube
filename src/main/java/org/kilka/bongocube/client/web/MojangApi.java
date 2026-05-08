package org.kilka.bongocube.client.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MojangApi {

    private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final String USERNAME_URL = "https://api.mojang.com/users/profiles/minecraft/";

    public static String getUuidFromUsername(String username) {
        try {
            URL url = new URL(USERNAME_URL + username);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            return jsonResponse.get("id").getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getSkinUrlFromUuid(String uuid) throws Exception {
        return SkinFetcher.fetchSkinUrl(PROFILE_URL + uuid);
    }
}