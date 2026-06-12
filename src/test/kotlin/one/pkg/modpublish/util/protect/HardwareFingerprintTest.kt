package one.pkg.modpublish.util.protect

import org.junit.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

class HardwareFingerprintTest {
    @Test
    fun testPerformance() {
        // Warm up
        for (i in 0..100) {
            HardwareFingerprint.getEnvironmentFingerprint()
        }

        val iterations = 1000
        val time = measureTimeMillis {
            for (i in 0..iterations) {
                HardwareFingerprint.getEnvironmentFingerprint()
            }
        }

        println("Performance of HardwareFingerprint: $time ms for $iterations iterations")
        assertTrue(time >= 0)
    }
}
