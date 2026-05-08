package org.kilka.bongocube.client.utils;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.kilka.bongocube.Bongocube;
import org.kilka.bongocube.client.Config;
import org.kilka.bongocube.client.ModExecutor;
import org.kilka.bongocube.client.events.ClickEvent;
import org.kilka.bongocube.client.web.MojangApi;
import org.kilka.bongocube.client.web.SkinDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChibiRenderer implements ClickEvent.TapListener {
    private static final Logger LOGGER = LoggerFactory.getLogger("Bongocube");

    private static final Map<String, ChibiSkin> skinCache = new HashMap<>();
    private static String currentPlayerName = "";

    private static float clickOffset = 0;

    public static void initialize() {
        TextureUtils.initialize();
    }

    public static Identifier render(GuiGraphics graphics, DeltaTracker tickCounter) {
        if (!Config.get().renderChibi) {
            return null;
        }

        double currentTime = Util.getMillis() * 2 / 1000.0;
        float lerpedAmount = Mth.abs(Mth.sin((float) currentTime) * 2);

        Identifier chibiId = getCurrentChibiId();
        if (chibiId == null) {
            chibiId = Identifier.fromNamespaceAndPath(Bongocube.MOD_ID, "textures/gui/bongocube_chibi.png");
        }

        int[] position = calculatePosition();
        int x = position[0];
        int y = position[1];

        int size = (int) (64 * Config.get().chibiScale);
        int offset = (64 - size) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, chibiId, x + offset, y + offset, 0, lerpedAmount+clickOffset, size, size, size, size);

        if (Util.getMillis()%5 == 0) clickOffset = 0;

        return chibiId;
    }

    public static int[] calculatePosition() {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int x, y;
        switch (Config.get().chibiPosition) {
            case BOTTOM_LEFT:
                x = screenWidth / 12;
                y = screenHeight - screenHeight / 7;
                break;
            case BOTTOM_RIGHT:
                x = screenWidth - screenWidth / 10;
                y = screenHeight - screenHeight / 7;
                break;
            case TOP_LEFT:
                x = screenWidth / 12;
                y = screenHeight / 8;
                break;
            case TOP_RIGHT:
                x = screenWidth - screenWidth / 10;
                y = screenHeight / 8;
                break;
            default:
                x = screenWidth / 12;
                y = screenHeight - screenHeight / 8;
        }
        return new int[]{x, y};
    }

    public static void updateCurrentPlayer() {
        String playerName = Config.get().chibiSkinPlayerName;

        if (playerName.isEmpty()) {
            currentPlayerName = "";
            return;
        }

        if (!playerName.equals(currentPlayerName)) {
            LOGGER.info("[ChibiRenderer] Player changed from '{}' to '{}'", currentPlayerName, playerName);
            currentPlayerName = playerName;
            loadSkinAsync(playerName);
        }
    }

    private static Identifier getCurrentChibiId() {
        updateCurrentPlayer();
        ChibiSkin skin = skinCache.get(currentPlayerName);
        if (skin != null) {
            return skin.textureId;
        }
        return null;
    }

    public static void loadSkinAsync(String playerName) {
        if (playerName.isEmpty()) {
            return;
        }

        ChibiSkin existingSkin = skinCache.get(playerName);
        if (existingSkin != null && existingSkin.textureId != null) {
            return;
        }

        Path modDir = FabricLoader.getInstance().getGameDir().resolve(Bongocube.MOD_ID);
        Path skinsDir = modDir.resolve("skins");
        String safeName = sanitizeFileName(playerName);

        boolean useSchema = Config.get().useChibiSchema;
        String suffix = useSchema ? "_chibi" : "_chibi_default";
        Path chibiPath = skinsDir.resolve(safeName.toLowerCase(java.util.Locale.ROOT) + suffix + ".png");

        final String finalSuffix = suffix;
        final String finalSafeName = safeName.toLowerCase(java.util.Locale.ROOT);

        CompletableFuture.runAsync(() -> {
            Identifier defaultId = Identifier.fromNamespaceAndPath(Bongocube.MOD_ID, "textures/gui/bongocube_chibi.png");

            try {
                if (safeName.isEmpty() || !safeName.matches("^[a-zA-Z0-9_-]+$")) {
                    return;
                }

                String textureName = finalSafeName + finalSuffix;
                Identifier textureId = Identifier.fromNamespaceAndPath(Bongocube.MOD_ID, "textures/gui/" + textureName);

                String uuid = MojangApi.getUuidFromUsername(playerName);
                if (uuid == null || uuid.isEmpty()) {
                    return;
                }

                if (Files.exists(chibiPath)) {
                    LOGGER.info("[ChibiRenderer] Loading from cache: {}", chibiPath);
                    Identifier result = TextureUtils.registerTextureFromPath(chibiPath, textureId);
                    skinCache.put(playerName, new ChibiSkin(result, useSchema));
                    return;
                }

                Path originalSkinPath = skinsDir.resolve(finalSafeName + "_original.png");
                try { Files.deleteIfExists(originalSkinPath); } catch (Exception ignored) {}

                String skinUrl;
                try {
                    skinUrl = MojangApi.getSkinUrlFromUuid(uuid);
                } catch (Exception e) {
                    return;
                }

                if (skinUrl == null) {
                    return;
                }

                NativeImage skinImage = SkinDownloader.downloadSkin(skinUrl, originalSkinPath);
                if (skinImage == null) {
                    return;
                }

                NativeImage chibiImage = useSchema ? TextureUtils.createChibiFromSkin(skinImage) : TextureUtils.createDefaultChibiOnly(skinImage);
                skinImage.close();

                if (chibiImage != null) {
                    TextureUtils.saveChibiImage(chibiImage, chibiPath);
                    Identifier result = TextureUtils.registerTexture(chibiImage, textureId, false).join();
                    chibiImage.close();
                    skinCache.put(playerName, new ChibiSkin(result, useSchema));
                    LOGGER.info("[ChibiRenderer] Created chibi for: {}", playerName);
                }

            } catch (Exception e) {
                LOGGER.error("[ChibiRenderer] Error loading skin for: {}", playerName, e);
            }
        }, ModExecutor.DOWNLOAD_EXECUTOR);
    }

    public static void rebuildAllSkins() {
        LOGGER.info("[ChibiRenderer] Starting rebuild of all chibi skins");
        Path modDir = FabricLoader.getInstance().getGameDir().resolve(Bongocube.MOD_ID);
        Path skinsDir = modDir.resolve("skins");

        if (!Files.exists(skinsDir)) {
            LOGGER.info("[ChibiRenderer] Skins directory does not exist");
            return;
        }

        skinCache.clear();

        try {
            var files = Files.list(skinsDir).toList();
            for (var file : files) {
                String name = file.getFileName().toString();
                if (name.endsWith("_original.png")) {
                    String playerName = name.replace("_original.png", "");
                    String safeName = sanitizeFileName(playerName);

                    LOGGER.info("[ChibiRenderer] Rebuilding: {}", playerName);

                    NativeImage skinImage = TextureUtils.parseImageFile(file);
                    if (skinImage == null) {
                        continue;
                    }

                    boolean useSchema = Config.get().useChibiSchema;
                    NativeImage chibiImage = useSchema ? TextureUtils.createChibiFromSkin(skinImage) : TextureUtils.createDefaultChibiOnly(skinImage);
                    skinImage.close();

                    if (chibiImage != null) {
                        String chibiFileName = safeName.toLowerCase(java.util.Locale.ROOT) + (useSchema ? "_chibi.png" : "_chibi_default.png");
                        Path chibiPath = skinsDir.resolve(chibiFileName);
                        TextureUtils.saveChibiImage(chibiImage, chibiPath);
                        chibiImage.close();
                    }
                }
            }
            LOGGER.info("[ChibiRenderer] Rebuild complete");
        } catch (Exception e) {
            LOGGER.error("[ChibiRenderer] Error during rebuild", e);
        }
    }

    public static void clearCache() {
        skinCache.clear();
    }

    public static void removeSkin(String playerName) {
        skinCache.remove(playerName);
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private static class ChibiSkin {
        public final Identifier textureId;
        public final boolean useSchema;

        public ChibiSkin(Identifier textureId, boolean useSchema) {
            this.textureId = textureId;
            this.useSchema = useSchema;
        }
    }

    @Override
    public void keyWasTapped() {
        clickOffset = 4;
    }
}