package benchmark

class WhenBytecodeInspection {

    fun exactDenseWhen(x: Int): Int {
        return when (x) {
            0 -> 10
            1 -> 11
            2 -> 12
            3 -> 13
            4 -> 14
            else -> -1
        }
    }

    fun exactSparseWhen(x: Int): Int {
        return when (x) {
            1 -> 10
            100 -> 20
            10_000 -> 30
            else -> -1
        }
    }

    fun rangeWhen(x: Int): Int {
        return when (x) {
            in 0..<4 -> 10
            in 4..<8 -> 20
            in 8..<12 -> 30
            else -> -1
        }
    }

    fun orderedThresholdWhen(x: Int): Int {
        return when {
            x < 0 -> -1
            x < 4 -> 10
            x < 8 -> 20
            x < 12 -> 30
            else -> -1
        }
    }

    fun ifElseThresholds(x: Int): Int {
        if (x < 0) return -1
        if (x < 4) return 10
        if (x < 8) return 20
        if (x < 12) return 30
        return -1
    }
}