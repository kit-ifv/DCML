package benchmark

import kotlin.random.Random

object VectorCheck {

    /**
     * Intentionally boring counted loop.
     *
     * Expected on x86-64:
     *   vectorized: vaddps / addps
     *   scalar:     vaddss / addss
     */
    @JvmStatic
    fun add(
        left: FloatArray,
        right: FloatArray,
        destination: FloatArray,
        size: Int,
    ) {
        var i = 0
        while (i < size) {
            destination[i] = left[i] + right[i]
            i++
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val size = 16 * 1024

        val left = FloatArray(size) { Random.nextFloat() }
        val right = FloatArray(size) { Random.nextFloat() }
        val destination = FloatArray(size)

        // Make the method hot enough to compile.
        repeat(20_000) {
            add(left, right, destination, size)
        }

        // Keep the result observable.
        var checksum = 0.0
        for (value in destination) {
            checksum += value
        }

        println("checksum = $checksum")
    }
}