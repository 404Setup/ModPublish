package one.pkg.modpublish.util.metadata

import org.junit.Test
import kotlin.test.assertEquals

class ModVersionTest {

    @Test
    fun testNormalizeVersionFormat() {
        val method = ModVersion::class.java.getDeclaredMethod("normalizeVersionFormat", String::class.java)
        method.isAccessible = true

        assertEquals("1.0.0", method.invoke(ModVersion, "1.0.0"))
        assertEquals("1.0.0-alpha", method.invoke(ModVersion, "1.0.0-alpha"))
        assertEquals("1.2.3-beta", method.invoke(ModVersion, "1.2.3-beta"))
        assertEquals("2.0.0-rc1", method.invoke(ModVersion, "2.0.0-rc1"))
        assertEquals("1.19.4-snapshot", method.invoke(ModVersion, "1.19.4-snapshot"))
        assertEquals("1.0.0", method.invoke(ModVersion, "1.0"))
        assertEquals("1.0.0", method.invoke(ModVersion, "1"))
        assertEquals("2.0.0-dev", method.invoke(ModVersion, "2.0.0-dev"))
        assertEquals("1.20.0-final", method.invoke(ModVersion, "1.20-final"))
        assertEquals("1.20.1-release", method.invoke(ModVersion, "1.20.1-release"))
    }
}
