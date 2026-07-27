package edu.kit.ifv.mobitopp.discretechoice.selection

/**
 * A weighted selection strategy that picks an index from a cumulative probability
 * distribution using binary search.
 *
 * Equivalent to [FloatWeightedSelection], but runs in O(log n) instead of O(n) by
 * binary-searching `probabilities` for the first entry >= [random].
 */
class FloatWeightedBinarySelection : FloatSelectionFunctionArray {
    /**
     * Selects an index from [probabilities] corresponding to the given [random] value.
     *
     * @param probabilities cumulative probabilities, sorted ascending with the final value `1.0`.
     * @param random a random value in `[0.0, 1.0)`.
     * @return the selected index, in `[0, probabilities.size - 1]`.
     */
    override fun pick(
        probabilities: FloatArray,
        random: Double,
    ): Int {
        var low = 0
        var high = probabilities.size

        while (low < high) {
            val middle = low + (high - low) / 2

            if (probabilities[middle] >= random) {
                high = middle
            } else {
                low = middle + 1
            }
        }
        return low
    }
}