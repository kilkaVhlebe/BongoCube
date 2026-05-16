package org.kilka.bongocube.client.screens.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.kilka.bongocube.client.utils.SkinChache;
import org.kilka.bongocube.net.data.BongocubeServerData;

import static org.kilka.bongocube.client.screens.MainScreen.ITEM_HEIGHT;


public class PlayerRowWidget extends AbstractWidget {
    private final BongocubeServerData.PlayerStats stats;
    private final String uuid;
    private final int rank;
    private final Identifier skinId;
    private final Font font;

    public PlayerRowWidget(int rank, BongocubeServerData.PlayerStats stats, String uuid, Font font) {
        super(0, 0, 300, ITEM_HEIGHT, Component.empty());
        this.rank = rank;
        this.stats = stats;
        this.uuid = uuid;
        this.skinId = SkinChache.get(uuid);
        this.font = font;
    }
    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        Identifier drawSkinId = skinId != null ? skinId : DefaultPlayerSkin.getDefaultTexture();
        graphics.drawString(font, String.valueOf(rank), getX() + 10, getY() + 5, 0xFFFFFFFF, false);
        PlayerFaceRenderer.draw(graphics, drawSkinId, getX() + 60, getY() + 5, 8, false, false, 0xFFFFFFFF);
        graphics.drawString(font, stats.playerName, getX() + 80, getY() + 5, 0xFFFFFFFF, false);
        graphics.drawString(font, String.valueOf(stats.clicks), getX() + 260, getY() + 5, 0xFFFFFFFF, false);
    }
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}