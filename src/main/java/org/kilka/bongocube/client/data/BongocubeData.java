package org.kilka.bongocube.client.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class BongocubeData {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Logger log = LoggerFactory.getLogger(BongocubeData.class);

    public long clicks = 0;

    public static BongocubeData create(Path configPath, String fileName) {
        Path configFile = configPath.resolve(fileName);

        try {
            if (Files.exists(configFile)) {
                return load(configFile);
            } else {
                BongocubeData config = new BongocubeData();
                save(config, configFile);
                return config;
            }
        } catch (Exception e) {
            log.error("Failed to load save, using defaults", e);
            return new BongocubeData();
        }
    }


    public void reload(Path configPath, String fileName) {
        Path configFile = configPath.resolve(fileName);
        if (Files.exists(configFile)) {
            try {
                BongocubeData loaded = load(configFile);
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

    private static BongocubeData load(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            String encoded = GSON.fromJson(reader, String.class);

            if (encoded == null || encoded.isEmpty()) {
                throw new IOException("Save file is empty or invalid");
            }

            String decoded = decode(encoded);
            DataWrapper wrapper = GSON.fromJson(decoded, DataWrapper.class);

            BongocubeData data = new BongocubeData();
            data.clicks = wrapper.clicks;

            return data;
        }
    }

    private static void save(BongocubeData config, Path path) throws IOException {
        Files.createDirectories(path.getParent());

        DataWrapper wrapper = new DataWrapper();
        wrapper.clicks = config.clicks;

        String json = GSON.toJson(wrapper);
        String encoded = encode(json);

        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(encoded, writer);
        }
    }

    public static BongocubeData loadOrCreate(Path path, String fileName) {
        try {
            return load(path.resolve(fileName));
        } catch (IOException e) {
            return create(path, fileName);
        }
    }

    private static String encode(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    private static String decode(String encoded) {
        byte[] decodedBytes = Base64.getDecoder().decode(encoded);
        return new String(decodedBytes);
    }

    private static class DataWrapper {
        public long clicks;
    }
}
