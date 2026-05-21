package benchmark

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 8, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Thread)
open class SearchBenchmark {

    @Param("4", "8", "16", "32", "64", "128", "256", "512", "1024", "4096")
    var size: Int = 0

    private lateinit var array: IntArray
    private lateinit var queries: IntArray
    private var queryIndex = 0

    @Setup
    fun setup() {
        array = IntArray(size) { it * 2 } // sorted: 0, 2, 4, ...

        val rnd = Random(123)
        queries = IntArray(4096) {
            if (it % 2 == 0) {
                // hits
                rnd.nextInt(size) * 2 + 1
                //array[rnd.nextInt(size)] If ever the hits need to be restored.
            } else {
                // misses between existing values
                rnd.nextInt(size) * 2 + 1
            }
        }
    }

    @Benchmark
    fun linearSearch(bh: Blackhole) {
        val q = nextQuery()
        bh.consume(linearSearch(array, q))
    }

    @Benchmark
    fun binarySearchStdlib(bh: Blackhole) {
        val q = nextQuery()
        bh.consume(array.binarySearch(q))
    }

    @Benchmark
    fun binarySearchManual(bh: Blackhole) {
        val q = nextQuery()
        bh.consume(binarySearch(array, q))
    }

    private fun nextQuery(): Int {
        val q = queries[queryIndex]
        queryIndex = (queryIndex + 1) and (queries.size - 1)
        return q
    }

    private fun linearSearch(a: IntArray, target: Int): Int {
        for (i in a.indices) {
            if (a[i] == target) return i
            if (a[i] > target) return -1 // sorted early exit
        }
        return -1
    }

    private fun binarySearch(a: IntArray, target: Int): Int {
        var low = 0
        var high = a.size - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = a[mid]

            when {
                midVal < target -> low = mid + 1
                midVal > target -> high = mid - 1
                else -> return mid
            }
        }

        return -1
    }
}