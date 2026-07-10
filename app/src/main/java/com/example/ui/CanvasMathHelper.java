package com.example.ui;

public class CanvasMathHelper {
    public static class PositionResult {
        public final float posX;
        public final float posY;

        public PositionResult(float posX, float posY) {
            this.posX = posX;
            this.posY = posY;
        }
    }

    /**
     * Adjusts the position of a canvas item when its scale changes, relative to an anchor point (opposite corner).
     */
    public static PositionResult adjustPositionForScale(
        float oldScale,
        float newScale,
        float posX,
        float posY,
        float widthPx,
        float heightPx,
        float rotationDegrees,
        float u,
        float v
    ) {
        float deltaScale = oldScale - newScale;
        double rotationRad = Math.toRadians(rotationDegrees);
        float cosVal = (float) Math.cos(rotationRad);
        float sinVal = (float) Math.sin(rotationRad);

        float dxDiff = u * widthPx * deltaScale;
        float dyDiff = v * heightPx * deltaScale;

        float dxRotatedDiff = dxDiff * cosVal - dyDiff * sinVal;
        float dyRotatedDiff = dxDiff * sinVal + dyDiff * cosVal;

        float newPosX = posX + (widthPx / 2f) * deltaScale + dxRotatedDiff;
        float newPosY = posY + (heightPx / 2f) * deltaScale + dyRotatedDiff;

        return new PositionResult(newPosX, newPosY);
    }
}
