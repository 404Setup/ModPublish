import kotlin.system.measureNanoTime

data class Loader(val curseForgeVersion: Int)

fun main() {
    val loaders = List(1000) { Loader(if (it % 2 == 0) it else 0) }

    // Warmup
    for (i in 1..10000) {
        loaders.filter { it.curseForgeVersion > 0 }.forEach { it.curseForgeVersion }
        loaders.forEach { if (it.curseForgeVersion > 0) it.curseForgeVersion }
    }

    val timeOld = measureNanoTime {
        for (i in 1..10000) {
            loaders.filter { it.curseForgeVersion > 0 }.forEach { it.curseForgeVersion }
        }
    }

    val timeNew = measureNanoTime {
        for (i in 1..10000) {
            loaders.forEach { if (it.curseForgeVersion > 0) it.curseForgeVersion }
        }
    }

    println("Baseline (filter.forEach): ${timeOld / 1_000_000.0} ms")
    println("Optimized (forEach + if): ${timeNew / 1_000_000.0} ms")
    println("Improvement: ${"%.2f".format((timeOld - timeNew).toDouble() / timeOld * 100)}%")
}
