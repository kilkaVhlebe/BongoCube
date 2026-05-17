package org.kilka.bongocube.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.kilka.bongocube.Bongocube;
import org.kilka.bongocube.client.screens.widgets.LabelWidget;
import org.kilka.bongocube.client.screens.widgets.PlayerRowWidget;
import org.kilka.bongocube.client.utils.SkinChache;
import org.kilka.bongocube.net.data.BongocubeServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainScreen extends Screen {
    private static final Logger log = LoggerFactory.getLogger(MainScreen.class);
    public static final int ITEM_HEIGHT = 20;

    private float scrollOffset = 0;
    private float maxScroll = 0;
    private final List<AbstractWidget> widgetRows = new ArrayList<>();
    private final int contentX = 40;
    private final int contentY = 40;
    public MainScreen(Component title) {
        super(title);
    }
    @Override
    protected void init() {
        int viewHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight() - 80;

        List<Map.Entry<String, BongocubeServerData.PlayerStats>> sorted =
                new ArrayList<>(Bongocube.playersStatsData.players.entrySet());
        sorted.sort((a, b) -> Long.compare(b.getValue().clicks, a.getValue().clicks));
        if (sorted.isEmpty()) {
            LabelWidget label = new LabelWidget(":(", this.font);
            label.setX(contentX);
            label.setY(contentY);
            this.addRenderableWidget(label);
            widgetRows.add(label);
        } else {
            int index = 1;
            int totalHeight = 0;

            for (Map.Entry<String, BongocubeServerData.PlayerStats> entry : sorted) {
                String uuid = entry.getKey();
                BongocubeServerData.PlayerStats stats = entry.getValue();
                SkinChache.preload(uuid);

                PlayerRowWidget row = new PlayerRowWidget(index, stats, uuid, this.font);
                row.setX(contentX);
                row.setY(contentY + (index - 1) * ITEM_HEIGHT);

                widgetRows.add(row);
                this.addRenderableWidget(row);
                index++;
                totalHeight += ITEM_HEIGHT;
            }

            maxScroll = Math.max(0, totalHeight - viewHeight);
            scrollOffset = Math.min(scrollOffset, maxScroll);
        }
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxScroll > 0) {
            scrollOffset += (float) (verticalAmount * -20);
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
            updateWidgetPositions();
        }
        return true;
    }
    private void updateWidgetPositions() {
        for (int i = 0; i < widgetRows.size(); i++) {
            AbstractWidget widget = widgetRows.get(i);
            widget.setY(contentY + i * ITEM_HEIGHT - (int)scrollOffset);
        }
    }
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        if (maxScroll > 0) {
            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            int scrollBarX = screenWidth - 20;
            int scrollBarY = contentY;
            int scrollBarHeight = screenHeight - 80;

            graphics.fill(scrollBarX, scrollBarY, scrollBarX + 8, scrollBarY + scrollBarHeight, 0xFF555555);

            float scrollRatio = scrollOffset / maxScroll;
            int thumbHeight = Math.max(20, (int)(scrollBarHeight * scrollBarHeight / (maxScroll + screenHeight)));
            int thumbY = scrollBarY + (int)(scrollRatio * (scrollBarHeight - thumbHeight));
            graphics.fill(scrollBarX, thumbY, scrollBarX + 8, thumbY + thumbHeight, 0xFFAAAAAA);
        }
    }
}