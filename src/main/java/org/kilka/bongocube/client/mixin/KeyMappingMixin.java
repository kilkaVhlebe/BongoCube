package org.kilka.bongocube.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.kilka.bongocube.client.BongocubeClient;
import org.kilka.bongocube.client.utils.ChibiRenderer;
import org.kilka.bongocube.net.c2s.ClicksDataPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;

import static org.kilka.bongocube.Bongocube.playersStatsData;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {

    private static final Logger log = LoggerFactory.getLogger(KeyMappingMixin.class);

    @WrapMethod(method = "set")
    private static void clickCounter(InputConstants.Key key, boolean held, Operation<Void> original) {
        if ( BongocubeClient.bongocubeData != null && !held && Minecraft.getInstance().player != null) {
            BongocubeClient.bongocubeData.clicks += 1;
            ChibiRenderer.keyWasTapped();
            ClientPlayNetworking.send(new ClicksDataPayload(BongocubeClient.bongocubeData.clicks, Minecraft.getInstance().player.getUUID()));
        }
        original.call(key, held);
    }

}
