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
 * val probabilities = doubleArrayOf(0.2, 0.5, 1.0)
 * val index = WeightedSelection().pick(probabilities, random = 0.6)
 * // index == 2
 * ```
 *
 * @return -1 if `probabilities.size == 0`
 */
class WeightedSelection: SelectionFunctionArray {
    override fun pick(probabilities: DoubleArray, random: Double): Int {
        for(i in probabilities.indices){
            if(probabilities[i] >= random){
                return i
            }
        }
        return probabilities.size - 1
    }
}