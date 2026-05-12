package org.kilka.bongocube.client.utils;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.Identifier;
import org.kilka.bongocube.client.web.MojangApi;
import org.kilka.bongocube.client.web.SkinDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SkinChache {

    private static final Map<String, Identifier> cache = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(SkinChache.class);

    public static void preload(String uuid) {
        if (cache.containsKey(uuid)) return;

        CompletableFuture.runAsync(() -> {
            try {
                String skinUrl = MojangApi.getSkinUrlFromUuid(uuid);
                if (skinUrl == null) return;

                NativeImage skin = SkinDownloader.downloadSkinToNativeImage(skinUrl);
                if (skin == null) return;

                Identifier id = Identifier.fromNamespaceAndPath("bongocube", "skins/" + uuid);
                TextureUtils.registerTexture(skin, id, false).join();
                cache.put(uuid, id);
            } catch (Exception e) {
                log.info("Preload interrupted: "+e);
            }
        });
    }

    public static Identifier get(String uuid) {
        return cache.get(uuid);
    }

    public static NativeImage SaveSkin(Path cachePath, byte[] data) throws IOException {
        if (Files.isRegularFile(cachePath, new LinkOption[0])) {
            try (InputStream is = Files.newInputStream(cachePath)) {
                return NativeImage.read(is);
            }
        }

        try {
            Files.createDirectories(cachePath.getParent());
            Files.write(cachePath, data);
            return NativeImage.read(new ByteArrayInputStream(data));
        } catch (IOException e) {
            throw new IOException("Skin saving interrupted", e);
        }
    }
}
