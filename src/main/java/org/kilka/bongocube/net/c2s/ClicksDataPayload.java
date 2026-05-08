package org.kilka.bongocube.net.c2s;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.kilka.bongocube.Bongocube;

public record ClicksDataPayload(Long clicks, String uuid) implements CustomPacketPayload {

    public static final Type<ClicksDataPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(Bongocube.MOD_ID, "clicks_data")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ClicksDataPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeLong(payload.clicks);
                        buf.writeUtf(payload.uuid);
                        },
                    (buf) -> new ClicksDataPayload(buf.readLong(), buf.readUtf())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
