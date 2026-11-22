package com.example.basescout;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Simple window: Scan button, sensitivity buttons, and one row per hotspot. */
public class ScoutScreen extends Screen {

    public ScoutScreen() {
        super(Component.literal("BaseScout"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 30;

        addRenderableWidget(new StringWidget(cx - 150, 10, 300, 12,
            Component.literal("BaseScout - passive base finder"), this.font));

        addRenderableWidget(Button.builder(Component.literal("Scan"), b -> {
            ScanLogic.run();
            this.rebuildWidgets();
        }).bounds(cx - 150, y, 100, 20).build());

        addRenderableWidget(Button.builder(Component.literal("-"), b -> {
            ScanLogic.minScore = Math.min(10, ScanLogic.minScore + 1); // stricter
            this.rebuildWidgets();
        }).bounds(cx - 40, y, 20, 20).build());

        addRenderableWidget(new StringWidget(cx - 16, y + 6, 70, 12,
            Component.literal("Sensitivity: " + (11 - Math.min(10, ScanLogic.minScore))), this.font));

        addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            ScanLogic.minScore = Math.max(1, ScanLogic.minScore - 1); // more sensitive
            this.rebuildWidgets();
        }).bounds(cx + 60, y, 20, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
            .bounds(cx + 90, y, 60, 20).build());

        y += 30;
        addRenderableWidget(new StringWidget(cx - 150, y, 300, 12,
            Component.literal(ScanLogic.lastStatus), this.font));
        y += 18;

        for (ScanLogic.Hotspot h : ScanLogic.lastResults) {
            String label = "X " + h.x() + "  Z " + h.z() + "   score " + h.score()
                + "   " + h.distance() + " blocks away";
            addRenderableWidget(new StringWidget(cx - 150, y + 6, 210, 12,
                Component.literal(label), this.font));
            addRenderableWidget(Button.builder(Component.literal("Copy"), b ->
                Minecraft.getInstance().keyboardHandler.setClipboard(h.x() + " " + h.z())
            ).bounds(cx + 90, y, 60, 20).build());
            y += 24;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false; // keep the game running while the window is open
    }
}
