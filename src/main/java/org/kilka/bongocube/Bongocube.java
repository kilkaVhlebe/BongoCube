package org.kilka.bongocube;

import net.fabricmc.api.ModInitializer;
import org.kilka.bongocube.client.data.PlayersStatsData;
import org.kilka.bongocube.net.ModNetworking;

public class Bongocube implements ModInitializer {

    public static String MOD_ID = "bongocube";

    public static PlayersStatsData playersStatsData = new PlayersStatsData();


    @Override
    public void onInitialize() {
        ModNetworking.init();
    }
}
