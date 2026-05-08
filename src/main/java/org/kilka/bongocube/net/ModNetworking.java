package org.kilka.bongocube.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;
import org.kilka.bongocube.Bongocube;
import org.kilka.bongocube.client.data.BongocubeData;
import org.kilka.bongocube.net.c2s.ClicksDataPayload;
import org.kilka.bongocube.net.data.BongocubeServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.UUID;

public class ModNetworking {

    private static final Logger log = LoggerFactory.getLogger(ModNetworking.class);

    private ModNetworking() {
    }

    @Nullable
    public static BongocubeServerData bongocubeServerData = null;

    public static void init() {
        PayloadTypeRegistry.playC2S().register(ClicksDataPayload.TYPE, ClicksDataPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ClicksDataPayload.TYPE, (payload, context) -> {

           long clicks = payload.clicks();
           ServerPlayer player = context.server().getPlayerList().getPlayer(UUID.fromString(payload.uuid()));
           Path serverModFolder = context.server().getServerDirectory().resolve("mods").resolve(Bongocube.MOD_ID);

           context.server().execute(() -> {
               if (player == null) return;

               if (context.server().isDedicatedServer()) {
                   bongocubeServerData = BongocubeServerData.loadOrCreate(serverModFolder, Bongocube.MOD_ID + ".json");
                   bongocubeServerData.setPlayerClicks(payload.uuid(), player.getName().getString(), clicks);
                   bongocubeServerData.save(serverModFolder, Bongocube.MOD_ID + ".json");
               } else {
                   bongocubeServerData = BongocubeServerData.loadOrCreate(
                           FabricLoader.getInstance().getConfigDir().resolve(Bongocube.MOD_ID).resolve("worlds_data"),
                           context.server().getWorldData().getLevelName() + ".json"
                           );
                   bongocubeServerData.setPlayerClicks(payload.uuid(), player.getName().getString(), clicks);
                   bongocubeServerData.save(
                           FabricLoader.getInstance().getConfigDir().resolve(Bongocube.MOD_ID).resolve("worlds_data"),
                           context.server().getWorldData().getLevelName() + ".json"
                   );
               }
           });
        });
    }
}
