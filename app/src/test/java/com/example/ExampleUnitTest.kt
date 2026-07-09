package com.example

import com.example.update.UpdateChecker
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testVersionComparison() {
    // Newer major
    assertTrue(UpdateChecker.isNewerVersion("1.0", "2.0"))
    // Newer minor
    assertTrue(UpdateChecker.isNewerVersion("1.0", "1.1"))
    // Newer patch
    assertTrue(UpdateChecker.isNewerVersion("1.0.0", "1.0.1"))
    // With 'v' prefix
    assertTrue(UpdateChecker.isNewerVersion("v1.0", "v1.2"))
    assertTrue(UpdateChecker.isNewerVersion("1.0", "v1.2"))
    assertTrue(UpdateChecker.isNewerVersion("v1.0", "1.2"))
    
    // Equal versions
    assertFalse(UpdateChecker.isNewerVersion("1.0", "1.0"))
    assertFalse(UpdateChecker.isNewerVersion("1.0.0", "1.0"))
    assertFalse(UpdateChecker.isNewerVersion("v1.0.0", "1.0"))

    // Older versions
    assertFalse(UpdateChecker.isNewerVersion("2.1", "2.0"))
    assertFalse(UpdateChecker.isNewerVersion("1.5", "1.4.9"))
    assertFalse(UpdateChecker.isNewerVersion("1.0.1", "1.0"))

    // Suffix versions (beta/rc/etc)
    assertTrue(UpdateChecker.isNewerVersion("1.0-beta", "1.1-rc1"))
    assertFalse(UpdateChecker.isNewerVersion("1.1-rc1", "1.0-beta"))
  }
}
