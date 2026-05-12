package org.kilka.bongocube.client.keymapings;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.kilka.bongocube.Bongocube;
import org.kilka.bongocube.client.screens.MainScreen;
import org.lwjgl.glfw.GLFW;

public class KeysMaps {

    private static KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(Bongocube.MOD_ID, "screens")
    );

    public static KeyMapping openCustomScreen = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                    "key.bongocube.open_screen",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_J,
                    CATEGORY
            ));

    public static void registerMappings() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openCustomScreen.consumeClick() && client.player !=null) {
                Minecraft.getInstance().setScreen(
                        new MainScreen(Component.empty())
                );
            }
        });
    }
}
