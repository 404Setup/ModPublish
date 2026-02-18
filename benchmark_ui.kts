
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.system.measureTimeMillis

// Mock data class
data class DependencyInfo(val id: String)

// Naive implementation
class NaivePanel : JPanel() {
    val dependencies = ArrayList<DependencyInfo>()
    val listPanel = JPanel()

    init {
        add(listPanel)
    }

    fun addDependency(dep: DependencyInfo) {
        dependencies.add(dep)
        refresh()
    }

    fun removeDependency(dep: DependencyInfo) {
        dependencies.remove(dep)
        refresh()
    }

    private fun refresh() {
        listPanel.removeAll()
        for (d in dependencies) {
            val p = JPanel()
            p.add(JLabel(d.id))
            listPanel.add(p)
        }
        listPanel.revalidate()
    }
}

// Optimized implementation
class OptimizedPanel : JPanel() {
    val dependencies = ArrayList<DependencyInfo>()
    val listPanel = JPanel()
    // Map to track components
    val dependencyMap = java.util.IdentityHashMap<DependencyInfo, JPanel>()

    init {
        add(listPanel)
    }

    fun addDependency(dep: DependencyInfo) {
        dependencies.add(dep)
        val p = JPanel()
        p.add(JLabel(dep.id))
        dependencyMap[dep] = p
        listPanel.add(p)
        listPanel.revalidate()
    }

    fun removeDependency(dep: DependencyInfo) {
        dependencies.remove(dep)
        val p = dependencyMap.remove(dep)
        if (p != null) {
            listPanel.remove(p)
            listPanel.revalidate()
        }
    }
}

fun main() {
    val count = 2000
    val dependencies = (1..count).map { DependencyInfo("dep-$it") }

    println("Benchmarking Naive Implementation with $count items...")
    val naivePanel = NaivePanel()
    val naiveAddTime = measureTimeMillis {
        for (dep in dependencies) {
            naivePanel.addDependency(dep)
        }
    }
    println("Naive Add Time: ${naiveAddTime}ms")

    val naiveRemoveTime = measureTimeMillis {
         // Removing from the end to avoid ArrayList shift costs dominating,
         // though we want to measure UI rebuild cost mainly.
         // Actually, let's remove from start to match worst case, or random?
         // Random removal or just iterating copy.
         val toRemove = ArrayList(dependencies)
         for (dep in toRemove) {
             naivePanel.removeDependency(dep)
         }
    }
    println("Naive Remove Time: ${naiveRemoveTime}ms")


    println("\nBenchmarking Optimized Implementation with $count items...")
    val optimizedPanel = OptimizedPanel()
    val optimizedAddTime = measureTimeMillis {
        for (dep in dependencies) {
            optimizedPanel.addDependency(dep)
        }
    }
    println("Optimized Add Time: ${optimizedAddTime}ms")

    val optimizedRemoveTime = measureTimeMillis {
         val toRemove = ArrayList(dependencies)
         for (dep in toRemove) {
             optimizedPanel.removeDependency(dep)
         }
    }
    println("Optimized Remove Time: ${optimizedRemoveTime}ms")
}
