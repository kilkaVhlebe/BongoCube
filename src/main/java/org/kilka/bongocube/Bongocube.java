package org.kilka.bongocube;

import net.fabricmc.api.ModInitializer;
import org.kilka.bongocube.net.ModNetworking;

public class Bongocube implements ModInitializer {

    public static String MOD_ID = "bongocube";

    @Override
    public void onInitialize() {
        ModNetworking.init();
    }
}
