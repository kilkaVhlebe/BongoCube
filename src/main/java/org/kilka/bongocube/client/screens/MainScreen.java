package org.kilka.bongocube.client.screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.kilka.bongocube.Bongocube;
import org.kilka.bongocube.client.screens.widgets.LabelWidget;
import org.kilka.bongocube.client.screens.widgets.PlayerRowWidget;
import org.kilka.bongocube.client.utils.SkinChache;
import org.kilka.bongocube.net.data.BongocubeServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
public class MainScreen extends Screen {
    private static final Logger log = LoggerFactory.getLogger(MainScreen.class);
    public static final int ITEM_HEIGHT = 20;
    private ScrollableLayout scrollableLayout;
    public MainScreen(Component title) {
        super(title);
    }
    @Override
    protected void init() {
        int viewHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight() - 40;
        List<Map.Entry<String, BongocubeServerData.PlayerStats>> sorted =
                new ArrayList<>(Bongocube.playersStatsData.players.entrySet());
        sorted.sort((a, b) -> Long.compare(b.getValue().clicks, a.getValue().clicks));
        LinearLayout contentLayout = LinearLayout.vertical().spacing(0);

        if (sorted.isEmpty()) {
            contentLayout.addChild(new LabelWidget(":(", this.font));
        }

        int index = 1;
        for (Map.Entry<String, BongocubeServerData.PlayerStats> entry : sorted) {
            String uuid = entry.getKey();
            BongocubeServerData.PlayerStats stats = entry.getValue();
            SkinChache.preload(uuid);
            PlayerRowWidget row = new PlayerRowWidget(index, stats, uuid, this.font);
            contentLayout.addChild(row);
            index++;
        }
        scrollableLayout = new ScrollableLayout(
                Minecraft.getInstance(),
                contentLayout,
                0
        );
        scrollableLayout.setMinWidth(this.width - 80);
        scrollableLayout.setMaxHeight(viewHeight);
        scrollableLayout.setX(40);
        scrollableLayout.setY(40);
        scrollableLayout.arrangeElements();
        scrollableLayout.visitWidgets(this::addRenderableWidget);
    }
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
    }
}