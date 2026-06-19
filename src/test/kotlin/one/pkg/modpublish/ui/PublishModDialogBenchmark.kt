package one.pkg.modpublish.ui

import org.junit.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

class PublishModDialogBenchmark {

    @Test
    fun benchmarkModTypeSelection() {
        val iterations = 100000

        // Mocking the types map
        val mockTypes = listOf(
            one.pkg.modpublish.data.internal.PublishType.Fabric,
            one.pkg.modpublish.data.internal.PublishType.Forge
        )

        // Mocking the checkboxes texts
        val checkboxTexts = listOf("Fabric", "Quilt", "Forge", "NeoForge", "Rift", "LiteLoader", "JavaAgent")

        // Baseline: The current implementation
        val timeOld = measureTimeMillis {
            for (i in 0 until iterations) {
                var selectedCount = 0
                checkboxTexts.forEach { text ->
                    val isSelected = mockTypes.contains(one.pkg.modpublish.data.internal.PublishType.valuesList.firstOrNull { it.displayName.equals(text.lowercase(), ignoreCase = true) })
                    if (isSelected) selectedCount++
                }
            }
        }

        // Optimized: Store PublishType with CheckBox, or use Map
        // Checkboxes should store the PublishType directly or use a Map.
        val checkboxTypes = checkboxTexts.mapIndexed { index, text ->
             one.pkg.modpublish.data.internal.PublishType.valuesList.firstOrNull { it.displayName.equals(text, ignoreCase = true) }
        }

        val timeNew = measureTimeMillis {
            for (i in 0 until iterations) {
                var selectedCount = 0
                checkboxTypes.forEach { type ->
                    val isSelected = type != null && mockTypes.contains(type)
                    if (isSelected) selectedCount++
                }
            }
        }

        println("Baseline time (simulated): ${timeOld}ms")
        println("Optimized time (simulated): ${timeNew}ms")
        assertTrue(timeNew < timeOld || timeNew < 50)
    }
}
