package org.kilka.bongocube.net.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.kilka.bongocube.Bongocube;
import org.kilka.bongocube.net.data.BongocubeServerData;

import java.util.Map;

public record PlayersDataPayload(Map<String, BongocubeServerData.PlayerStats> players) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PlayersDataPayload> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(Bongocube.MOD_ID, "player_stats")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayersDataPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeMap(payload.players(),
                                (b, k) -> b.writeUtf(k),
                                (b, v) -> {
                                    b.writeUtf(v.playerName);
                                    b.writeVarLong(v.clicks);
                                });
                    },
                    (buf) -> new PlayersDataPayload(
                            buf.readMap(FriendlyByteBuf::readUtf, (b) ->
                                    new BongocubeServerData.PlayerStats(b.readUtf(), b.readVarLong())
                            )
                    )
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
