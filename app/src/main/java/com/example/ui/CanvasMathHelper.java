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

    /**
     * Rotates drag coordinates according to a given rotation angle in degrees.
     */
    public static PositionResult rotateDragAmount(float dragX, float dragY, float rotationDegrees) {
        double rotationRad = Math.toRadians(rotationDegrees);
        float cosVal = (float) Math.cos(rotationRad);
        float sinVal = (float) Math.sin(rotationRad);

        float parentDragX = dragX * cosVal - dragY * sinVal;
        float parentDragY = dragX * sinVal + dragY * cosVal;

        return new PositionResult(parentDragX, parentDragY);
    }

    /**
     * Calculates Euclidean distance between two points in 2D space.
     */
    public static float calculateDistance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calculates the new scale ratio based on initial scale and distance ratios.
     */
    public static float calculateNewScale(float startScale, float startDist, float currentDist, float minScale, float maxScale) {
        if (startDist <= 0.1f) return startScale;
        float scale = startScale * (currentDist / startDist);
        if (scale < minScale) return minScale;
        if (scale > maxScale) return maxScale;
        return scale;
    }
}

