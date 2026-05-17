package org.kilka.bongocube.client.screens.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kilka.bongocube.client.screens.MainScreen.ITEM_HEIGHT;

public class LabelWidget extends AbstractWidget {
    private static final Logger log = LoggerFactory.getLogger(LabelWidget.class);

    private final String labelText;
    private final Font font;

    public LabelWidget(String labelText, Font font) {
        super(0, 0, 300, ITEM_HEIGHT, Component.empty());
        this.labelText = labelText;
        this.font = font;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.text(font, labelText, getX() + 10, getY() + 5, 0xFFFFFFFF, false);

    }
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}