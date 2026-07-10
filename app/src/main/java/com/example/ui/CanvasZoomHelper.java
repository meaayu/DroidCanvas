package com.example.ui;

public class CanvasZoomHelper {

    public static class ZoomResult {
        public final float nextScale;
        public final float nextTranslateX;
        public final float nextTranslateY;

        public ZoomResult(float nextScale, float nextTranslateX, float nextTranslateY) {
            this.nextScale = nextScale;
            this.nextTranslateX = nextTranslateX;
            this.nextTranslateY = nextTranslateY;
        }
    }

    /**
     * Calculates the new scale and translation offsets for zooming into a centroid point.
     *
     * @param oldScale Current scale of the canvas
     * @param zoomChange Proposed multiplicative zoom factor
     * @param panX Horizontal translation delta from pan gesture
     * @param panY Vertical translation delta from pan gesture
     * @param currentTranslateX Current horizontal offset of the canvas
     * @param currentTranslateY Current vertical offset of the canvas
     * @param centroidX Horizontal screen position of the zoom centroid
     * @param centroidY Vertical screen position of the zoom centroid
     * @param minScale Minimum scale boundary (e.g., 0.01f)
     * @param maxScale Maximum scale boundary (e.g., 10.0f)
     * @return ZoomResult with new scale and translation coordinates, or null if zoom cannot be performed
     */
    public static ZoomResult computeZoom(
            float oldScale,
            float zoomChange,
            float panX,
            float panY,
            float currentTranslateX,
            float currentTranslateY,
            float centroidX,
            float centroidY,
            float minScale,
            float maxScale
    ) {
        if ((oldScale >= maxScale && zoomChange > 1f) || (oldScale <= minScale && zoomChange < 1f)) {
            return null;
        }

        float targetScale = oldScale * zoomChange;
        if (targetScale < minScale) targetScale = minScale;
        if (targetScale > maxScale) targetScale = maxScale;

        float effectiveZoomFactor = targetScale / oldScale;

        float nextTranslateX = (currentTranslateX + panX) * effectiveZoomFactor + centroidX * (1f - effectiveZoomFactor);
        float nextTranslateY = (currentTranslateY + panY) * effectiveZoomFactor + centroidY * (1f - effectiveZoomFactor);

        return new ZoomResult(targetScale, nextTranslateX, nextTranslateY);
    }
}
