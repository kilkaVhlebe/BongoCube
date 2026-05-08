package org.kilka.bongocube.client.utils;

import java.util.List;

public class ChibiModel {
    private final List<PixelMapping> mappings;
    private final int width;
    private final int height;

    public ChibiModel(List<PixelMapping> mappings, int width, int height) {
        this.mappings = mappings;
        this.width = width;
        this.height = height;
    }

    public List<PixelMapping> getMappings() {
        return mappings;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static class PixelMapping {
        private final int fromX;
        private final int fromY;
        private final int toX;
        private final int toY;

        public PixelMapping(int fromX, int fromY, int toX, int toY) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        public int getFromX() { return fromX; }
        public int getFromY() { return fromY; }
        public int getToX() { return toX; }
        public int getToY() { return toY; }
    }
}