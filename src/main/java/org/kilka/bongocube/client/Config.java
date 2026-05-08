package org.kilka.bongocube.client;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.*;


public class Config {

    public static final ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(YACLPlatform.getConfigDir().resolve("bongocube_config.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();

    public static void initialize() {
        load();
    }

    public static Config get() {
        return HANDLER.instance();
    }

    public static void save() {
        HANDLER.save();
    }

    public static void load() {
        HANDLER.load();
    }

    public static Screen createScreen(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("BongoCube"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Settings"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("General"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Counter enabled"))
                                        .binding(
                                                true, () -> Config.get().renderCounter, newVal -> Config.get().renderCounter = newVal
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Chibi enabled"))
                                        .binding(
                                                true, () -> Config.get().renderChibi, newVal -> Config.get().renderChibi = newVal
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Use custom chibi schema"))
                                        .binding(
                                                true, () -> Config.get().useChibiSchema, newVal -> Config.get().useChibiSchema = newVal
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<ChibiPosition>createBuilder()
                                        .name(Component.literal("Chibi enabled"))
                                        .binding(
                                                ChibiPosition.BOTTOM_LEFT, () -> Config.get().chibiPosition, newVal -> Config.get().chibiPosition = newVal
                                        )
                                        .controller(opt -> EnumControllerBuilder.create(opt)
                                                .enumClass(ChibiPosition.class))
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(Component.literal("Counter color"))
                                        .binding(
                                                new Color(0xFFFFFF), () -> Config.get().counterColor, newVal -> Config.get().counterColor = newVal
                                        )
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build())
                                .option(Option.<String>createBuilder()
                                        .name(Component.literal("Chibi skin by player name"))
                                        .binding(
                                                "", () -> Config.get().chibiSkinPlayerName, newVal -> Config.get().chibiSkinPlayerName = newVal
                                        )
                                        .controller(StringControllerBuilder::create)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.literal("Chibi scale"))
                                        .binding(
                                                1, () -> (int)Math.round(Config.get().chibiScale * 10), newVal -> Config.get().chibiScale = newVal / 10.0
                                        )
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(5, 40)
                                                .step(1)
                                                .formatValue(v -> Component.nullToEmpty(v / 10.0 + "x")))
                                        .build())
.option(ButtonOption.createBuilder()
                                        .name(Component.literal("Rebuild all chibi skins"))
                                        .text(Component.literal("Rebuild"))
                                        .action(button -> {
                                            org.kilka.bongocube.client.utils.ChibiRenderer.rebuildAllSkins();
                                        })
                                        .build())
                                .build())
                        .build())
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }

    @SerialEntry
    public boolean renderCounter = true;

    @SerialEntry
    public boolean renderChibi = true;

    @SerialEntry
    public ChibiPosition chibiPosition = ChibiPosition.BOTTOM_LEFT;

    public enum ChibiPosition implements NameableEnum {
        TOP_LEFT,
        BOTTOM_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT;

        @Override
        public Component getDisplayName() {
            return Component.translatable(name());
        }
    }

    @SerialEntry
    public Color counterColor = new Color(0xFFFFFF);

    @SerialEntry
    public String chibiSkinPlayerName = "";

@SerialEntry
    public double chibiScale = 1.0;

    @SerialEntry
    public boolean useChibiSchema = true;
}
