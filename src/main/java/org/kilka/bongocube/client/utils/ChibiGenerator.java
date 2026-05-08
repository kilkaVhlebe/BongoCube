package org.kilka.bongocube.client.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.platform.NativeImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ChibiGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("Bongocube");
    private static final Gson GSON = new GsonBuilder().create();

    private ChibiModel model;
    private boolean loaded = false;
    private double scale = 1.0;

    public void setScale(double scale) {
        this.scale = scale;
    }

    public boolean load(Path modDir) {
        Path jsonPath = modDir.resolve("chibi_schema.json");

        if (!Files.exists(jsonPath)) {
            return false;
        }

        try {
            String content = new String(Files.readAllBytes(jsonPath));
            JsonSchema schema = GSON.fromJson(content, JsonSchema.class);

            if (schema.mappings == null || schema.mappings.isEmpty()) {
                LOGGER.error("[ChibiGenerator] No mappings found in JSON");
                return false;
            }

            List<ChibiModel.PixelMapping> mappings = schema.mappings.stream()
                    .map(m -> new ChibiModel.PixelMapping(m.fromX, m.fromY, m.toX, m.toY))
                    .toList();

            model = new ChibiModel(mappings, schema.width, schema.height);
            loaded = true;

            LOGGER.info("[ChibiGenerator] Loaded schema from JSON: {}x{}, {} mappings",
                    model.getWidth(), model.getHeight(), model.getMappings().size());
            return true;
        } catch (Exception e) {
            LOGGER.error("[ChibiGenerator] Failed to load JSON schema", e);
            return false;
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public NativeImage generateChibi(NativeImage skinImage) {
        if (!loaded || model == null) {
            return null;
        }

        int baseWidth = model.getWidth();
        int baseHeight = model.getHeight();
        int targetWidth = (int) (baseWidth * scale);
        int targetHeight = (int) (baseHeight * scale);

        NativeImage chibi = new NativeImage(targetWidth, targetHeight, true);

        for (ChibiModel.PixelMapping mapping : model.getMappings()) {
            int fromX = mapping.getFromX();
            int fromY = mapping.getFromY();
            int toX = (int) (mapping.getToX() * scale);
            int toY = (int) (mapping.getToY() * scale);

            if (fromX >= 0 && fromX < skinImage.getWidth() &&
                fromY >= 0 && fromY < skinImage.getHeight()) {
                int pixel = skinImage.getPixel(fromX, fromY);

                // Масштабирование пикселя
                int pixelSize = Math.max(1, (int) scale);
                for (int dy = 0; dy < pixelSize; dy++) {
                    for (int dx = 0; dx < pixelSize; dx++) {
                        int px = toX + dx;
                        int py = toY + dy;
                        if (px >= 0 && px < targetWidth && py >= 0 && py < targetHeight) {
                            chibi.setPixel(px, py, pixel);
                        }
                    }
                }
            }
        }

        LOGGER.info("[ChibiGenerator] Generated chibi: {}x{} (scale={})", targetWidth, targetHeight, scale);
        return chibi;
    }

    private static class JsonSchema {
        @SerializedName("width")
        int width;

        @SerializedName("height")
        int height;

        @SerializedName("mappings")
        List<JsonMapping> mappings;
    }

    private static class JsonMapping {
        @SerializedName("fromX")
        int fromX;

        @SerializedName("fromY")
        int fromY;

        @SerializedName("toX")
        int toX;

        @SerializedName("toY")
        int toY;
    }
}