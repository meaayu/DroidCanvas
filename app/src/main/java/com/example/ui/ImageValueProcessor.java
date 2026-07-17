package com.example.ui;

import android.graphics.Bitmap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageValueProcessor {
    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    public static class ProcessingResult {
        public final Bitmap processedBitmap;
        public final int[] histogram;
        public final float[] distribution;

        public ProcessingResult(Bitmap processedBitmap, int[] histogram, float[] distribution) {
            this.processedBitmap = processedBitmap;
            this.histogram = histogram;
            this.distribution = distribution;
        }
    }

    /**
     * Highly optimized, multi-threaded posterization (values study) process.
     */
    public static ProcessingResult processImage(
            Bitmap source,
            int simplicity,
            float[] stopPositions,
            int[] stopColors
    ) {
        int width = source.getWidth();
        int height = source.getHeight();
        
        Bitmap blurred = source;
        if (simplicity > 0) {
            int radius = simplicity / 4;
            if (radius > 0) {
                blurred = parallelBoxBlur(source, radius);
            }
        }

        int[] pixels = new int[width * height];
        blurred.getPixels(pixels, 0, width, 0, 0, width, height);

        int[] outputPixels = new int[width * height];
        final int[] histogram = new int[256];
        final int[] stopCounts = new int[stopPositions.length];

        int numStops = stopPositions.length;
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xff;
            int g = (pixel >> 8) & 0xff;
            int b = pixel & 0xff;

            float l = 0.2126f * r + 0.7152f * g + 0.0722f * b;
            int lInt = (int) l;
            if (lInt < 0) lInt = 0;
            if (lInt > 255) lInt = 255;
            histogram[lInt]++;

            float lNorm = l / 255f;
            int matchedIndex = -1;
            for (int s = 0; s < numStops; s++) {
                if (stopPositions[s] >= lNorm) {
                    matchedIndex = s;
                    break;
                }
            }
            if (matchedIndex == -1) {
                matchedIndex = numStops - 1;
            }

            stopCounts[matchedIndex]++;
            outputPixels[i] = stopColors[matchedIndex];
        }

        Bitmap dest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        dest.setPixels(outputPixels, 0, width, 0, 0, width, height);

        float totalPixels = (float) pixels.length;
        float[] distribution = new float[numStops];
        for (int i = 0; i < numStops; i++) {
            distribution[i] = totalPixels > 0 ? (stopCounts[i] / totalPixels) * 100f : 0f;
        }

        return new ProcessingResult(dest, histogram, distribution);
    }

    /**
     * Parallel Box Blur implementation utilizing multiple CPU cores.
     */
    private static Bitmap parallelBoxBlur(Bitmap src, final int radius) {
        final int width = src.getWidth();
        final int height = src.getHeight();
        final int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);

        final int[] outPixels = new int[width * height];
        final int numThreads = Math.min(NUM_CORES, height);
        final int rowsPerThread = (height + numThreads - 1) / numThreads;

        // Pass 1: Horizontal Blur (Parallel)
        Thread[] threads = new Thread[numThreads];
        for (int t = 0; t < numThreads; t++) {
            final int startY = t * rowsPerThread;
            final int endY = Math.min(startY + rowsPerThread, height);
            threads[t] = new Thread(new Runnable() {
                @Override
                public void run() {
                    int rLimit = Math.min(radius, width - 1);
                    for (int y = startY; y < endY; y++) {
                        int rSum = 0, gSum = 0, bSum = 0;
                        int rowOffset = y * width;

                        for (int x = -rLimit; x <= rLimit; x++) {
                            int clampX = x;
                            if (clampX < 0) clampX = 0;
                            if (clampX > width - 1) clampX = width - 1;
                            int px = pixels[rowOffset + clampX];
                            rSum += (px >> 16) & 0xff;
                            gSum += (px >> 8) & 0xff;
                            bSum += px & 0xff;
                        }

                        int count = 2 * rLimit + 1;
                        for (int x = 0; x < width; x++) {
                            outPixels[rowOffset + x] = (0xff << 24) |
                                    ((rSum / count) << 16) |
                                    ((gSum / count) << 8) |
                                    (bSum / count);

                            int prevX = x - rLimit;
                            if (prevX < 0) prevX = 0;
                            int nextX = x + rLimit + 1;
                            if (nextX > width - 1) nextX = width - 1;

                            int prevPx = pixels[rowOffset + prevX];
                            int nextPx = pixels[rowOffset + nextX];

                            rSum += ((nextPx >> 16) & 0xff) - ((prevPx >> 16) & 0xff);
                            gSum += ((nextPx >> 8) & 0xff) - ((prevPx >> 8) & 0xff);
                            bSum += (nextPx & 0xff) - (prevPx & 0xff);
                        }
                    }
                }
            });
            threads[t].start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Pass 2: Vertical Blur (Parallel)
        final int[] finalPixels = new int[width * height];
        final int numColThreads = Math.min(NUM_CORES, width);
        final int colsPerThread = (width + numColThreads - 1) / numColThreads;

        threads = new Thread[numColThreads];
        for (int t = 0; t < numColThreads; t++) {
            final int startX = t * colsPerThread;
            final int endX = Math.min(startX + colsPerThread, width);
            threads[t] = new Thread(new Runnable() {
                @Override
                public void run() {
                    int ryLimit = Math.min(radius, height - 1);
                    for (int x = startX; x < endX; x++) {
                        int rSum = 0, gSum = 0, bSum = 0;

                        for (int y = -ryLimit; y <= ryLimit; y++) {
                            int clampY = y;
                            if (clampY < 0) clampY = 0;
                            if (clampY > height - 1) clampY = height - 1;
                            int px = outPixels[clampY * width + x];
                            rSum += (px >> 16) & 0xff;
                            gSum += (px >> 8) & 0xff;
                            bSum += px & 0xff;
                        }

                        int count = 2 * ryLimit + 1;
                        for (int y = 0; y < height; y++) {
                            finalPixels[y * width + x] = (0xff << 24) |
                                    ((rSum / count) << 16) |
                                    ((gSum / count) << 8) |
                                    (bSum / count);

                            int prevY = y - ryLimit;
                            if (prevY < 0) prevY = 0;
                            int nextY = y + ryLimit + 1;
                            if (nextY > height - 1) nextY = height - 1;

                            int prevPx = outPixels[prevY * width + x];
                            int nextPx = outPixels[nextY * width + x];

                            rSum += ((nextPx >> 16) & 0xff) - ((prevPx >> 16) & 0xff);
                            gSum += ((nextPx >> 8) & 0xff) - ((prevPx >> 8) & 0xff);
                            bSum += (nextPx & 0xff) - (prevPx & 0xff);
                        }
                    }
                }
            });
            threads[t].start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Bitmap dest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        dest.setPixels(finalPixels, 0, width, 0, 0, width, height);
        return dest;
    }
}
