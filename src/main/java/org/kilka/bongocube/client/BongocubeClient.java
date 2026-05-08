package org.kilka.bongocube.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.kilka.bongocube.Bongocube;
import org.kilka.bongocube.client.data.BongocubeData;
import org.kilka.bongocube.client.utils.ChibiRenderer;
import org.kilka.bongocube.client.utils.ChibiSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class BongocubeClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Bongocube");

    @Nullable
    public static BongocubeData bongocubeData = null;

    @Override
    public void onInitializeClient() {
        Config.initialize();
        ChibiRenderer.initialize();

        Path modDir = FabricLoader.getInstance().getGameDir().resolve(Bongocube.MOD_ID);
        ChibiSchema schema = new ChibiSchema();
        if (schema.load(modDir)) {
            LOGGER.info("[BongocubeClient] Chibi schema loaded from PNG files");
        }

        ClientPlayConnectionEvents.JOIN.register((listener, sender, minecraft) -> {
            bongocubeData = BongocubeData.loadOrCreate(
                    FabricLoader.getInstance().getGameDir().resolve(Bongocube.MOD_ID),
                    getFileName(minecraft)
            );
        });

        ClientPlayConnectionEvents.DISCONNECT.register((listener, minecraft) -> {
            if (bongocubeData != null) {
                bongocubeData.save(
                        FabricLoader.getInstance().getGameDir().resolve(Bongocube.MOD_ID),
                        getFileName(minecraft)
                );
                bongocubeData = null;
            }
        });

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(Bongocube.MOD_ID, "before_chat"),
                BongocubeClient::render
        );
    }

    private String getFileName(Minecraft minecraft) {
        String levelName;
        if (minecraft.isLocalServer()) {
            levelName = minecraft.getSingleplayerServer().getWorldData().getLevelName();
        } else {
            ServerData server = minecraft.getCurrentServer();
            levelName = server != null ? server.ip : "unknown";
        }
        return levelName + ".json";
    }

    private static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        ChibiRenderer.render(graphics, tickCounter);

        if (bongocubeData != null && Config.get().renderCounter) {
            int[] position = ChibiRenderer.calculatePosition();
            int x = position[0];
            int y = position[1];
            graphics.drawString(
                    Minecraft.getInstance().font,
                    String.valueOf(bongocubeData.clicks),
                    x + 5,
                    y - 5,
                    Config.get().counterColor.getRGB()
            );
        }
    }
}