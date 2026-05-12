package org.kilka.bongocube.net;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;
import org.kilka.bongocube.Bongocube;
import org.kilka.bongocube.net.c2s.ClicksDataPayload;
import org.kilka.bongocube.net.data.BongocubeServerData;
import org.kilka.bongocube.net.s2c.PlayersDataPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.kilka.bongocube.Bongocube.playersStatsData;

public class ModNetworking {

    private static final Logger log = LoggerFactory.getLogger(ModNetworking.class);

    private static Path dataPath;
    private static String fileName;

    private ModNetworking() {
    }

    @Nullable
    public static BongocubeServerData bongocubeServerData = null;

    public static void init() {
        PayloadTypeRegistry.playC2S().register(ClicksDataPayload.TYPE, ClicksDataPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PlayersDataPayload.TYPE, PlayersDataPayload.STREAM_CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {

            if (server.isDedicatedServer()) {
                dataPath = server.getServerDirectory().resolve(Bongocube.MOD_ID);
                fileName = "player_stats.json";
            } else {
                dataPath = FabricLoader.getInstance().getGameDir()
                        .resolve(Bongocube.MOD_ID).resolve("worlds_data");
                fileName = server.getWorldData().getLevelName() + ".json";
            }

            bongocubeServerData = BongocubeServerData.loadOrCreate(dataPath, fileName);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register( server -> {
            if (bongocubeServerData!=null) {

                if (server.isDedicatedServer()) {
                    dataPath = server.getServerDirectory().resolve(Bongocube.MOD_ID);
                    fileName = "player_stats.json";
                } else {
                    dataPath = FabricLoader.getInstance().getGameDir()
                            .resolve(Bongocube.MOD_ID).resolve("worlds_data");
                    fileName = server.getWorldData().getLevelName() + ".json";
                }
                bongocubeServerData.save(dataPath,fileName);
            }
        });

        AtomicInteger tickCounter = new AtomicInteger();
        tickCounter.set(0);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter.getAndIncrement();

            if (tickCounter.get() >= 300 * 20) {

                if (bongocubeServerData!=null) {

                    if (server.isDedicatedServer()) {
                        dataPath = server.getServerDirectory().resolve(Bongocube.MOD_ID);
                        fileName = "player_stats.json";
                    } else {
                        dataPath = FabricLoader.getInstance().getGameDir()
                                .resolve(Bongocube.MOD_ID).resolve("worlds_data");
                        fileName = server.getWorldData().getLevelName() + ".json";
                    }
                    bongocubeServerData.saveIfDirty(dataPath, fileName);

                }
                tickCounter.set(0);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(ClicksDataPayload.TYPE, (payload, context) -> {

           long clicks = payload.clicks();
           ServerPlayer player = context.server().getPlayerList().getPlayer(payload.uuid());

           context.server().execute(() -> {
               if (player == null) return;

               if (bongocubeServerData !=null) {
                   bongocubeServerData.setPlayerClicks(payload.uuid().toString(), player.getName().getString(), clicks);
                   ServerPlayNetworking.send(player,new PlayersDataPayload(bongocubeServerData.players));
               } else {
                   log.warn("Received click data but server not ready");
               }
           });
        });


        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(PlayersDataPayload.TYPE, (payload, context) -> {
                ClientLevel level = context.client().level;

                if (level == null) {
                    return;
                }

                playersStatsData.players = payload.players();

            });
        }
    }
}
