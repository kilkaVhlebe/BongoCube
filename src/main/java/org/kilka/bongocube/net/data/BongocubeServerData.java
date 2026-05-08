package org.kilka.bongocube.net.data;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class BongocubeServerData {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Logger log = LoggerFactory.getLogger(BongocubeServerData.class);

    public Map<String, PlayerStats> players = new LinkedHashMap<>();

    public static BongocubeServerData create(Path configPath, String fileName) {
        Path configFile = configPath.resolve(fileName);

        try {
            if (Files.exists(configFile)) {
                return load(configFile);
            } else {
                BongocubeServerData config = new BongocubeServerData();
                save(config, configFile);
                return config;
            }
        } catch (Exception e) {
            log.error("Failed to load save, using defaults", e);
            return new BongocubeServerData();
        }
    }


    public void reload(Path configPath, String fileName) {
        Path configFile = configPath.resolve(fileName);
        if (Files.exists(configFile)) {
            try {
                BongocubeServerData loaded = load(configFile);
                log.info("[BGC] Save reloaded successfully.");
            } catch (Exception e) {
                log.error("Failed to reload config", e);
            }
        }
    }

    public void save(Path configDir, String fileName) {
        Path configFile = configDir.resolve(fileName);
        try {
            save(this, configFile);
        } catch (IOException e) {
            log.error("Failed to save data", e);
        }
    }

    private static BongocubeServerData load(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            BongocubeServerData config = GSON.fromJson(reader, BongocubeServerData.class);
            if (config == null) {
                throw new IOException("Save file is empty or invalid");
            }
            return config;
        }
    }

    private static void save(BongocubeServerData config, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(config, writer);
        }
    }

    public static BongocubeServerData loadOrCreate(Path path, String fileName) {
        try {
            return load(path.resolve(fileName));
        } catch (IOException e) {
            return create(path, fileName);
        }
    }

    public void setPlayerClicks(String uuid, String playerName, long clicks) {
        players.computeIfAbsent(uuid, k -> new PlayerStats(playerName, 0));
        players.get(uuid).clicks = clicks;
    }

    public long getPlayerClicks(String uuid) {
        return players.getOrDefault(uuid, new PlayerStats()).clicks;
    }

    public class PlayerStats {
        public String playerName;
        public long clicks;

        public PlayerStats() {}
        public PlayerStats(String playerName, long clicks) {
            this.playerName = playerName;
            this.clicks = clicks;
        }
    }
}


