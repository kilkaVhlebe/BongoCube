package org.kilka.bongocube.client.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Base64;

public class SkinFetcher {

    public static String fetchSkinUrl(String profileUrl) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create(profileUrl)
                    .toURL()
                    .openConnection(Minecraft.getInstance().getProxy());

            connection.setRequestProperty("User-Agent", "Mozilla/5.0 Minecraft Client");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoInput(true);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return null;
            }

            String response = new String(connection.getInputStream().readAllBytes());
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

            if (!jsonResponse.has("properties")) {
                return null;
            }

            var properties = jsonResponse.getAsJsonArray("properties");
            for (var prop : properties) {
                JsonObject property = prop.getAsJsonObject();
                if ("textures".equals(property.get("name").getAsString())) {
                    String value = property.get("value").getAsString();
                    String decoded = new String(Base64.getDecoder().decode(value));
                    JsonObject textures = JsonParser.parseString(decoded).getAsJsonObject();

                    if (textures.has("textures") && textures.getAsJsonObject("textures").has("SKIN")) {
                        JsonObject skin = textures.getAsJsonObject("textures").getAsJsonObject("SKIN");
                        return skin.get("url").getAsString();
                    }
                }
            }

            return null;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}