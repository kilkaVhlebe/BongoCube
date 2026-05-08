package org.kilka.bongocube.client.web;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class SkinDownloader {

    public static NativeImage downloadSkin(String skinUrl, Path cachePath) throws IOException {
        if (Files.isRegularFile(cachePath, new LinkOption[0])) {
            try (InputStream is = Files.newInputStream(cachePath)) {
                return NativeImage.read(is);
            }
        }

        HttpURLConnection connection = null;
        int maxRetries = 3;
        int currentTry = 0;
        IOException lastException = null;

        while (currentTry < maxRetries) {
            try {
                connection = (HttpURLConnection) URI.create(skinUrl)
                        .toURL()
                        .openConnection(Minecraft.getInstance().getProxy());

                connection.setRequestProperty("User-Agent", "Mozilla/5.0 Minecraft Client");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setDoInput(true);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    byte[] data = connection.getInputStream().readAllBytes();

                    try {
                        Files.createDirectories(cachePath.getParent());
                        Files.write(cachePath, data);
                    } catch (IOException e) {
                    }

                    return NativeImage.read(new ByteArrayInputStream(data));
                }

                currentTry++;
                Thread.sleep(1000);

            } catch (IOException e) {
                lastException = e;
                currentTry++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Download interrupted", e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Download interrupted");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        throw new IOException("Failed to download skin after " + maxRetries + " attempts", lastException);
    }
}