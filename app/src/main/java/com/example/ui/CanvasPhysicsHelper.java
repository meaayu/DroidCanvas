package com.example.ui;

public class CanvasPhysicsHelper {

    public static class PhysicsStepResult {
        public final float nextVelocityX;
        public final float nextVelocityY;
        public final float nextTranslateX;
        public final float nextTranslateY;

        public PhysicsStepResult(float nextVelocityX, float nextVelocityY, float nextTranslateX, float nextTranslateY) {
            this.nextVelocityX = nextVelocityX;
            this.nextVelocityY = nextVelocityY;
            this.nextTranslateX = nextTranslateX;
            this.nextTranslateY = nextTranslateY;
        }
    }

    /**
     * Computes the next state of a physics fling motion.
     *
     * @param velocityX Current velocity on X axis
     * @param velocityY Current velocity on Y axis
     * @param currentTranslateX Current translation on X axis
     * @param currentTranslateY Current translation on Y axis
     * @param dtMilli Time delta in milliseconds
     * @param friction Friction coefficient (e.g., 0.95f)
     * @return PhysicsStepResult with updated velocities and positions
     */
    public static PhysicsStepResult processDecay(
            float velocityX,
            float velocityY,
            float currentTranslateX,
            float currentTranslateY,
            long dtMilli,
            double friction
    ) {
        double exponent = (double) dtMilli / 16.0;
        float decay = (float) Math.pow(friction, exponent);

        float nextVelocityX = velocityX * decay;
        float nextVelocityY = velocityY * decay;

        float dtSec = (float) dtMilli / 1000f;
        float nextTranslateX = currentTranslateX + nextVelocityX * dtSec;
        float nextTranslateY = currentTranslateY + nextVelocityY * dtSec;

        return new PhysicsStepResult(nextVelocityX, nextVelocityY, nextTranslateX, nextTranslateY);
    }
}
