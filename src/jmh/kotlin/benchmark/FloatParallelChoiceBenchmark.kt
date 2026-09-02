package benchmark

import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.random.Random
import kotlin.system.measureNanoTime

fun main() {
    val alternativeCount = 96
    val benchmarkIterations = 1_000_000
    val threadCount = max(1, Runtime.getRuntime().availableProcessors())
    val seed = 42

    val localAttributes =
        Impedance.random(size = alternativeCount)

    val iterationsPerThread =
        benchmarkIterations / threadCount

    val remainder =
        benchmarkIterations % threadCount

    val checksums =
        LongArray(threadCount)

    val combinedNanoseconds = measureNanoTime {
        val workers =
            List(threadCount) { threadIndex ->
                thread {
                    val threadIterations =
                        iterationsPerThread +
                                if (threadIndex < remainder) 1 else 0

                    val random =
                        Random(seed + threadIndex)

                    val attributes =
                        Attributes.random()

                    val choiceModel =
                        TestFloatBatchModel(localAttributes)

                    var localChecksum = 0L

                    repeat(threadIterations) {
                        attributes.randomize(random)

                        val selectedIndex =
                            with(attributes) {
                                with(random) {
                                    choiceModel.select()
                                }
                            }

                        localChecksum += selectedIndex.toLong()
                    }

                    checksums[threadIndex] =
                        localChecksum
                }
            }

        workers.forEach { it.join() }
    }

    val selectionChecksum =
        checksums.sum()

    val combinedTotalMs =
        combinedNanoseconds / 1_000_000.0

    val wallTimePerChoiceMs =
        combinedTotalMs / benchmarkIterations

    val choicesPerSecond =
        benchmarkIterations /
                (combinedTotalMs / 1_000.0)

    println("Threads: $threadCount")
    println("Total wall time: $combinedTotalMs ms")
    println("Wall time per choice: $wallTimePerChoiceMs ms")
    println("Choices per second: $choicesPerSecond")
    println("Checksum: $selectionChecksum")
}