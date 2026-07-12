package com.example

import com.example.ui.StrokeOptimizer
import org.junit.Assert.*
import org.junit.Test

class StrokeOptimizerTest {

    @Test
    fun testSimplifyNoOpForFewPoints() {
        val xs = floatArrayOf(0f, 10f)
        val ys = floatArrayOf(0f, 10f)
        val result = StrokeOptimizer.simplify(xs, ys, 1f)
        
        assertArrayEquals(xs, result[0], 0.001f)
        assertArrayEquals(ys, result[1], 0.001f)
    }

    @Test
    fun testSimplifyCollinearPoints() {
        // Collinear points: (0,0), (5,5), (10,10)
        val xs = floatArrayOf(0f, 5f, 10f)
        val ys = floatArrayOf(0f, 5f, 10f)
        // With epsilon = 1f, the middle point should be simplified away
        val result = StrokeOptimizer.simplify(xs, ys, 1f)
        
        assertArrayEquals(floatArrayOf(0f, 10f), result[0], 0.001f)
        assertArrayEquals(floatArrayOf(0f, 10f), result[1], 0.001f)
    }

    @Test
    fun testChaikinSmoothing() {
        val xs = floatArrayOf(0f, 10f, 20f)
        val ys = floatArrayOf(0f, 10f, 0f)
        val result = StrokeOptimizer.smooth(xs, ys, 1)
        
        // Expected size for 3 inputs is 2 * 3 - 2 = 4
        assertEquals(4, result[0].size)
        // End points remain unchanged
        assertEquals(0f, result[0][0], 0.001f)
        assertEquals(20f, result[0][3], 0.001f)
    }

    @Test
    fun testStrokeIntersectingPoint() {
        val xs = floatArrayOf(0f, 10f)
        val ys = floatArrayOf(0f, 10f)
        
        // A point exactly on the segment
        assertTrue(StrokeOptimizer.isStrokeIntersectingPoint(xs, ys, 5f, 5f, 1f))
        
        // A point close to the segment
        assertTrue(StrokeOptimizer.isStrokeIntersectingPoint(xs, ys, 5f, 6f, 1.5f))
        
        // A point far from the segment
        assertFalse(StrokeOptimizer.isStrokeIntersectingPoint(xs, ys, 5f, 20f, 2f))
    }
}
