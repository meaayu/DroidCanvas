package com.example.ui;

public class StrokeOptimizer {

    /**
     * Ramer-Douglas-Peucker (RDP) algorithm to simplify a 2D path.
     * Returns a 2D array: [0] for x coordinates, [1] for y coordinates.
     */
    public static float[][] simplify(float[] x, float[] y, float epsilon) {
        if (x == null || y == null || x.length < 3) {
            return new float[][]{x, y};
        }

        int n = x.length;
        boolean[] keep = new boolean[n];
        keep[0] = true;
        keep[n - 1] = true;

        simplifyStep(x, y, 0, n - 1, epsilon, keep);

        // Count kept points
        int count = 0;
        for (boolean k : keep) {
            if (k) count++;
        }

        float[] rx = new float[count];
        float[] ry = new float[count];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            if (keep[i]) {
                rx[idx] = x[i];
                ry[idx] = y[i];
                idx++;
            }
        }

        return new float[][]{rx, ry};
    }

    private static void simplifyStep(float[] x, float[] y, int start, int end, float epsilon, boolean[] keep) {
        if (end - start < 2) {
            return;
        }

        float x1 = x[start];
        float y1 = y[start];
        float x2 = x[end];
        float y2 = y[end];

        float maxDistSq = 0;
        int maxIndex = -1;

        float dx = x2 - x1;
        float dy = y2 - y1;
        float lenSq = dx * dx + dy * dy;

        for (int i = start + 1; i < end; i++) {
            float distSq;
            if (lenSq == 0) {
                float ndx = x[i] - x1;
                float ndy = y[i] - y1;
                distSq = ndx * ndx + ndy * ndy;
            } else {
                float t = ((x[i] - x1) * dx + (y[i] - y1) * dy) / lenSq;
                if (t < 0) t = 0;
                else if (t > 1) t = 1;
                
                float projX = x1 + t * dx;
                float projY = y1 + t * dy;
                float ndx = x[i] - projX;
                float ndy = y[i] - projY;
                distSq = ndx * ndx + ndy * ndy;
            }

            if (distSq > maxDistSq) {
                maxDistSq = distSq;
                maxIndex = i;
            }
        }

        float epsilonSq = epsilon * epsilon;
        if (maxDistSq > epsilonSq && maxIndex != -1) {
            keep[maxIndex] = true;
            simplifyStep(x, y, start, maxIndex, epsilon, keep);
            simplifyStep(x, y, maxIndex, end, epsilon, keep);
        }
    }

    /**
     * Chaikin's Corner Cutting algorithm for curve smoothing.
     * Generates a smooth, organic stroke from raw, jagged input coordinates.
     */
    public static float[][] smooth(float[] x, float[] y, int iterations) {
        if (x == null || y == null || x.length < 3) {
            return new float[][]{x, y};
        }

        float[] currX = x;
        float[] currY = y;

        for (int iter = 0; iter < iterations; iter++) {
            int n = currX.length;
            if (n < 3) {
                break;
            }
            int outLen = 2 * n - 2;
            float[] nextX = new float[outLen];
            float[] nextY = new float[outLen];

            // Keep original first point
            nextX[0] = currX[0];
            nextY[0] = currY[0];

            int idx = 1;
            for (int i = 0; i < n - 1; i++) {
                float px = currX[i];
                float py = currY[i];
                float nx = currX[i + 1];
                float ny = currY[i + 1];

                if (i == 0) {
                    nextX[idx] = 0.25f * px + 0.75f * nx;
                    nextY[idx] = 0.25f * py + 0.75f * ny;
                    idx++;
                } else if (i == n - 2) {
                    nextX[idx] = 0.75f * px + 0.25f * nx;
                    nextY[idx] = 0.75f * py + 0.25f * ny;
                    idx++;
                } else {
                    nextX[idx] = 0.75f * px + 0.25f * nx;
                    nextY[idx] = 0.75f * py + 0.25f * ny;
                    idx++;
                    nextX[idx] = 0.25f * px + 0.75f * nx;
                    nextY[idx] = 0.25f * py + 0.75f * ny;
                    idx++;
                }
            }

            // Keep original last point
            nextX[outLen - 1] = currX[n - 1];
            nextY[outLen - 1] = currY[n - 1];

            currX = nextX;
            currY = nextY;
        }

        return new float[][]{currX, currY};
    }

    /**
     * Unified pipeline: first simplifies using RDP to remove jitter and redundant points,
     * then smooths using Chaikin's corner cutting.
     */
    public static float[][] optimizeStroke(float[] x, float[] y, float epsilon, int smoothIterations) {
        float[][] simplified = simplify(x, y, epsilon);
        return smooth(simplified[0], simplified[1], smoothIterations);
    }

    /**
     * Checks if a point (px, py) is within threshold distance of any segment of the stroke.
     * Implemented in Java for premium-grade, sub-millisecond precision eraser operations.
     */
    public static boolean isStrokeIntersectingPoint(float[] xs, float[] ys, float px, float py, float threshold) {
        if (xs == null || ys == null || xs.length == 0) {
            return false;
        }
        if (xs.length == 1) {
            float dx = xs[0] - px;
            float dy = ys[0] - py;
            return (dx * dx + dy * dy) < (threshold * threshold);
        }

        float thresholdSq = threshold * threshold;
        for (int i = 0; i < xs.length - 1; i++) {
            float x1 = xs[i];
            float y1 = ys[i];
            float x2 = xs[i + 1];
            float y2 = ys[i + 1];

            float dx = x2 - x1;
            float dy = y2 - y1;
            float lenSq = dx * dx + dy * dy;

            float distSq;
            if (lenSq == 0) {
                float ndx = px - x1;
                float ndy = py - y1;
                distSq = ndx * ndx + ndy * ndy;
            } else {
                float t = ((px - x1) * dx + (py - y1) * dy) / lenSq;
                if (t < 0) t = 0;
                else if (t > 1) t = 1;

                float projX = x1 + t * dx;
                float projY = y1 + t * dy;
                float ndx = px - projX;
                float ndy = py - projY;
                distSq = ndx * ndx + ndy * ndy;
            }

            if (distSq < thresholdSq) {
                return true;
            }
        }
        return false;
    }
}
