package org.kilka.bongocube.client.screens.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import static org.kilka.bongocube.client.screens.MainScreen.ITEM_HEIGHT;

public class LabelWidget extends AbstractWidget {
    private final String labelText;
    private final Font font;

    public LabelWidget(String labelText, Font font) {
        super(0, 0, 300, ITEM_HEIGHT, Component.empty());
        this.labelText = labelText;
        this.font = font;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.drawString(font, labelText, getX() + 10, getY() + 5, 0xFFFFFFFF, false);

    }
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}