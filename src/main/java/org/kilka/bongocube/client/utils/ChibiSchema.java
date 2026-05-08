package org.kilka.bongocube.client.utils;

import com.mojang.blaze3d.platform.NativeImage;
import org.kilka.bongocube.Bongocube;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChibiSchema {
    private static final Logger LOGGER = LoggerFactory.getLogger("Bongocube");

    private ChibiModel model;
    private boolean loaded = false;

    public boolean load(Path modDir) {
        Path schemaPath = modDir.resolve("chibi_schema.png");
        Path commonSkinPath = modDir.resolve("common_skin.png");

        if (!Files.exists(schemaPath)) {
            LOGGER.info("[ChibiSchema] Schema file not found: {}", schemaPath);
            return false;
        }

        if (!Files.exists(commonSkinPath)) {
            LOGGER.info("[ChibiSchema] Common skin file not found: {}", commonSkinPath);
            return false;
        }

        try {
            NativeImage schemaImage = NativeImage.read(Files.newInputStream(schemaPath));
            NativeImage commonSkinImage = NativeImage.read(Files.newInputStream(commonSkinPath));

            LOGGER.info("[ChibiSchema] Loading schema: {}x{}, common skin: {}x{}",
                    schemaImage.getWidth(), schemaImage.getHeight(),
                    commonSkinImage.getWidth(), commonSkinImage.getHeight());

            model = parseSchema(schemaImage, commonSkinImage);

            schemaImage.close();
            commonSkinImage.close();

            if (model != null) {
                loaded = true;
                saveToJson(modDir);
                LOGGER.info("[ChibiSchema] Schema loaded successfully with {} mappings", model.getMappings().size());
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("[ChibiSchema] Failed to load schema", e);
        }

        return false;
    }

    private ChibiModel parseSchema(NativeImage schemaImage, NativeImage commonSkinImage) {
        Map<Integer, int[]> colorToFromPos = new HashMap<>();
        int commonWidth = commonSkinImage.getWidth();
        int commonHeight = commonSkinImage.getHeight();

        for (int y = 0; y < commonHeight; y++) {
            for (int x = 0; x < commonWidth; x++) {
                int pixel = commonSkinImage.getPixel(x, y);
                int alpha = (pixel >> 24) & 0xFF;
                if (alpha > 0) {
                    colorToFromPos.put(pixel, new int[]{x, y});
                }
            }
        }

        List<ChibiModel.PixelMapping> mappings = new ArrayList<>();
        int schemaWidth = schemaImage.getWidth();
        int schemaHeight = schemaImage.getHeight();

        for (int y = 0; y < schemaHeight; y++) {
            for (int x = 0; x < schemaWidth; x++) {
                int pixel = schemaImage.getPixel(x, y);
                int alpha = (pixel >> 24) & 0xFF;
                if (alpha > 0) {
                    int[] fromPos = colorToFromPos.get(pixel);
                    if (fromPos != null) {
                        mappings.add(new ChibiModel.PixelMapping(fromPos[0], fromPos[1], x, y));
                    }
                }
            }
        }

        return new ChibiModel(mappings, schemaWidth, schemaHeight);
    }

    private void saveToJson(Path modDir) {
        Path jsonPath = modDir.resolve("chibi_schema.json");
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"width\": ").append(model.getWidth()).append(",\n");
            sb.append("  \"height\": ").append(model.getHeight()).append(",\n");
            sb.append("  \"mappings\": [\n");

            List<ChibiModel.PixelMapping> mappings = model.getMappings();
            for (int i = 0; i < mappings.size(); i++) {
                ChibiModel.PixelMapping m = mappings.get(i);
                sb.append("    {\"fromX\": ").append(m.getFromX())
                        .append(", \"fromY\": ").append(m.getFromY())
                        .append(", \"toX\": ").append(m.getToX())
                        .append(", \"toY\": ").append(m.getToY()).append("}");
                if (i < mappings.size() - 1) sb.append(",");
                sb.append("\n");
            }

            sb.append("  ]\n");
            sb.append("}\n");

            Files.write(jsonPath, sb.toString().getBytes());
            LOGGER.info("[ChibiSchema] Schema saved to: {}", jsonPath);
        } catch (IOException e) {
            LOGGER.error("[ChibiSchema] Failed to save JSON", e);
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public ChibiModel getModel() {
        return model;
    }
}