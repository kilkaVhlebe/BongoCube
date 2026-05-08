package org.kilka.bongocube.client.utils;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.Identifier;
import org.kilka.bongocube.Bongocube;
import org.kilka.bongocube.client.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class TextureUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger("Bongocube");

    private static final ChibiGenerator chibiGenerator = new ChibiGenerator();
    private static boolean schemaLoaded = false;

    public static void initialize() {
        Path modDir = FabricLoader.getInstance().getGameDir().resolve(Bongocube.MOD_ID);
        schemaLoaded = chibiGenerator.load(modDir);

        if (schemaLoaded) {
            LOGGER.info("[TextureUtils] Chibi schema loaded successfully");
        } else {
            LOGGER.info("[TextureUtils] No chibi schema found, using default generation");
        }
    }

    private static void updateScale() {
        if (chibiGenerator != null) {
            chibiGenerator.setScale(Config.get().chibiScale);
        }
    }

    public static CompletableFuture<Identifier> registerTexture(Path path, Identifier identifier, boolean remap) {
        NativeImage image = parseImageFile(path);
        if (image != null) {
            return registerTexture(image, identifier, remap);
        } else {
            return CompletableFuture.completedFuture(MissingTextureAtlasSprite.getLocation());
        }
    }

    public static CompletableFuture<Identifier> registerTexture(NativeImage image, Identifier identifier, boolean remap) {
        LOGGER.info("[registerTexture] Registering texture: {}, image size: {}x{}, remap: {}", identifier, image.getWidth(), image.getHeight(), remap);
        CompletableFuture<Identifier> future = new CompletableFuture<>();

        try {
            if (remap) {
                image = remapTexture(image);
            }

            NativeImage finalImage = image;
            Minecraft.getInstance().execute(() -> {
                try {
                    DynamicTexture texture = new DynamicTexture(identifier::toString, finalImage);
                    Minecraft.getInstance().getTextureManager().register(identifier, texture);
                    LOGGER.info("[registerTexture] Successfully registered on main thread: {}", identifier);
                    future.complete(identifier);
                } catch (Exception e) {
                    LOGGER.error("[registerTexture] Failed to register on main thread", e);
                    future.complete(Identifier.fromNamespaceAndPath(Bongocube.MOD_ID, "textures/gui/bongocube_chibi.png"));
                }
            });

        } catch (Exception e) {
            LOGGER.error("[registerTexture] Failed to register texture", e);
            future.complete(Identifier.fromNamespaceAndPath(Bongocube.MOD_ID, "textures/gui/bongocube_chibi.png"));
        }

        return future;
    }

    public static void unregisterTexture(Identifier identifier) {
        Minecraft.getInstance().getTextureManager().release(identifier);
    }

    public static NativeImage parseImageFile(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return NativeImage.read(inputStream);
        } catch (Exception e) {
            return null;
        }
    }

    public static NativeImage getNativeImage(Identifier identifier) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(identifier);
        if (texture instanceof DynamicTexture nativeImageBackedTexture) {
            return nativeImageBackedTexture.getPixels();
        } else if (texture instanceof SimpleTexture resourceTexture) {
            try {
                return resourceTexture.loadContents(Minecraft.getInstance().getResourceManager()).image();
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static void saveImage(Identifier identifier, Path path) {
        NativeImage image = getNativeImage(identifier);
        if (image != null) {
            saveImage(image, path);
        }
    }

    public static void saveImage(NativeImage image, Path path) {
        try {
            image.writeToFile(path.toFile());
        } catch (Exception e) {
        }
    }

    private static NativeImage remapTexture(NativeImage image) {
        int i = image.getHeight();
        int j = image.getWidth();
        if (j == 64 && (i == 32 || i == 64)) {
            boolean bl = i == 32;
            if (bl) {
                NativeImage nativeImage = new NativeImage(64, 64, true);
                nativeImage.copyFrom(image);
                image.close();
                image = nativeImage;
                nativeImage.fillRect(0, 32, 64, 32, 0);
                nativeImage.copyRect(4, 16, 16, 32, 4, 4, true, false);
                nativeImage.copyRect(8, 16, 16, 32, 4, 4, true, false);
                nativeImage.copyRect(0, 20, 24, 32, 4, 12, true, false);
                nativeImage.copyRect(4, 20, 16, 32, 4, 12, true, false);
                nativeImage.copyRect(8, 20, 8, 32, 4, 12, true, false);
                nativeImage.copyRect(12, 20, 16, 32, 4, 12, true, false);
                nativeImage.copyRect(44, 16, -8, 32, 4, 4, true, false);
                nativeImage.copyRect(48, 16, -8, 32, 4, 4, true, false);
                nativeImage.copyRect(40, 20, 0, 32, 4, 12, true, false);
                nativeImage.copyRect(44, 20, -8, 32, 4, 12, true, false);
                nativeImage.copyRect(48, 20, -16, 32, 4, 12, true, false);
                nativeImage.copyRect(52, 20, -8, 32, 4, 12, true, false);
            }
            stripAlpha(image, 0, 0, 32, 16);
            if (bl) {
                stripColor(image, 32, 0, 64, 32);
            }

            stripAlpha(image, 0, 16, 64, 32);
            stripAlpha(image, 16, 48, 48, 64);
            return image;
        } else {
            image.close();
            return null;
        }
    }

    private static void stripColor(NativeImage image, int x1, int y1, int x2, int y2) {
        for(int i = x1; i < x2; ++i) {
            for(int j = y1; j < y2; ++j) {
                int k = image.getPixel(i, j);
                if ((k >> 24 & 255) < 128) {
                    return;
                }
            }
        }

        for(int i = x1; i < x2; ++i) {
            for(int j = y1; j < y2; ++j) {
                image.setPixel(i, j, image.getPixel(i, j) & 16777215);
            }
        }
    }

    private static void stripAlpha(NativeImage image, int x1, int y1, int x2, int y2) {
        for(int i = x1; i < x2; ++i) {
            for(int j = y1; j < y2; ++j) {
                image.setPixel(i, j, image.getPixel(i, j) | -16777216);
            }
        }
    }

    public static NativeImage createChibiFromSkin(NativeImage skinImage) {
        int width = skinImage.getWidth();
        int height = skinImage.getHeight();
        LOGGER.info("[createChibiFromSkin] Input: {}x{}", width, height);

        if (width != 64 || (height != 32 && height != 64)) {
            LOGGER.warn("[createChibiFromSkin] Invalid skin size");
            return null;
        }

        if (Config.get().useChibiSchema && schemaLoaded && chibiGenerator.isLoaded()) {
            updateScale();
            NativeImage chibi = chibiGenerator.generateChibi(skinImage);
            if (chibi != null) {
                return chibi;
            }
            LOGGER.warn("[createChibiFromSkin] Schema generation failed, falling back to default");
        }

        return createDefaultChibi(skinImage, height);
    }

    public static NativeImage createDefaultChibiOnly(NativeImage skinImage) {
        int height = skinImage.getHeight();
        return createDefaultChibi(skinImage, height);
    }

    private static NativeImage createDefaultChibi(NativeImage skinImage, int height) {
        NativeImage chibi = new NativeImage(64, 64, true);

        int offset = 16;
        int scale = 4;

        for (int py = 0; py < 8; py++) {
            for (int px = 0; px < 8; px++) {
                int pixel = skinImage.getPixel(8 + px, 8 + py);
                for (int dy = 0; dy < scale; dy++) {
                    for (int dx = 0; dx < scale; dx++) {
                        chibi.setPixel(offset + px * scale + dx, offset + py * scale + dy, pixel);
                    }
                }
            }
        }

        if (height == 64) {
            for (int py = 0; py < 8; py++) {
                for (int px = 0; px < 8; px++) {
                    int pixel = skinImage.getPixel(40 + px, 8 + py);
                    int alpha = (pixel >> 24) & 0xFF;
                    if (alpha > 0) {
                        for (int dy = 0; dy < scale; dy++) {
                            for (int dx = 0; dx < scale; dx++) {
                                chibi.setPixel(offset + px * scale + dx, offset + py * scale + dy, pixel);
                            }
                        }
                    }
                }
            }
        }

        LOGGER.info("[createChibiFromSkin] Created default chibi 32x32 centered in 64x64");
        return chibi;
    }

    public static Identifier registerTextureFromPath(Path path, Identifier identifier) {
        LOGGER.info("[registerTextureFromPath] Loading from: {}, identifier: {}", path, identifier);
        NativeImage image = parseImageFile(path);
        if (image == null) {
            LOGGER.warn("[registerTextureFromPath] Failed to parse image from: {}", path);
            return Identifier.fromNamespaceAndPath(Bongocube.MOD_ID, "textures/gui/bongocube_chibi.png");
        }
        LOGGER.info("[registerTextureFromPath] Image loaded, size: {}x{}", image.getWidth(), image.getHeight());
        return registerTexture(image, identifier, false).join();
    }

    public static void saveChibiImage(NativeImage image, Path path) {
        try {
            Files.createDirectories(path.getParent());
            image.writeToFile(path.toFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}