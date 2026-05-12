package org.kilka.bongocube.net.c2s;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.kilka.bongocube.Bongocube;

import java.util.UUID;

public record ClicksDataPayload(Long clicks, UUID uuid) implements CustomPacketPayload {

    public static final Type<ClicksDataPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(Bongocube.MOD_ID, "clicks_data")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ClicksDataPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeLong(payload.clicks);
                        buf.writeUUID(payload.uuid);
                        },
                    (buf) -> new ClicksDataPayload(buf.readLong(), buf.readUUID())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
