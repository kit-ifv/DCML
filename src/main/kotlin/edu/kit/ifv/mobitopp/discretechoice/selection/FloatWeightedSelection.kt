package edu.kit.ifv.mobitopp.discretechoice.selection

/**
 * A weighted selection strategy that picks an index from a set of cumulative
 * probabilities based on a given random value.
 *
 * This implementation expects `probabilities` to represent a **cumulative
 * distribution** (i.e., each entry is the running total of weights up to and
 * including that index, typically normalized so the final entry equals `1.0`),
 * rather than raw/individual weights.
 *
 * The selection works by scanning the array in order and returning the index
 * of the first entry whose cumulative probability is greater than or equal to
 * [random]. This effectively selects an index proportionally to the weight
 * assigned to it in the cumulative distribution.
 *
 * Example:
 * ```
 * // Cumulative probabilities for weights [0.2, 0.3, 0.5]
 * val probabilities = floatArrayOf(0.2f, 0.5f, 1.0f)
 * val index = FloatWeightedSelection().pick(probabilities, random = 0.6)
 * // index == 2
 * ```
 */
class FloatWeightedSelection: FloatSelectionFunctionArray {
    /**
     * Selects an index from [probabilities] corresponding to the given [random] value.
     *
     * Iterates through [probabilities] in order and returns the index of the first
     * element greater than or equal to [random]. If no such element is found
     * (which should only happen due to floating-point rounding when [random] is
     * very close to `1.0`), the index of the last element is returned as a fallback.
     *
     * @param probabilities an array of cumulative probabilities, expected to be
     *                      sorted in ascending order with the final value equal to `1.0`.
     * @param random a random value, in the range `[0.0, 1.0)`, used to
     *               select an index.
     * @return the selected index, in the range `[0, probabilities.size - 1]`.
     */
    override fun pick(probabilities: FloatArray, random: Double): Int {
        for(i in probabilities.indices){
            if(probabilities[i] >= random){
                return i
            }
        }
        return probabilities.size - 1
    }
}