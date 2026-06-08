package edu.kit.ifv.mobitopp.discretechoice.distribution

import kotlin.math.exp

/**
 * Converts log-space utilities into cumulative choice probabilities in place.
 *
 * Implementations read  utilities as raw utility values and overwrite it with cumulative
 * probabilities for random selection.
 *
 * Returns `true` if a valid cumulative distribution was produced. Returns `false` if the
 * utilities cannot produce positive finite probability mass, for example because all relevant
 * values underflow to zero. In that case, the caller is expected to use a fallback strategy.
 */
fun interface CumulateDistributionArray<in P> {
    fun tryCumulateProbabilities(utilities: DoubleArray, parameters: P): Boolean
}


fun <P> CumulateDistributionArray<P>.probabilities(utilities: DoubleArray, parameters: P): DoubleArray {
    val success = tryCumulateProbabilities(utilities, parameters)
    if(!success) { throw IllegalStateException("Cannot generate probabilities due to error in calculation, the result" +
            " would be useless.")
    }
    val probs = DoubleArray(utilities.size)


    if (utilities.isNotEmpty()) {
        probs[0] = utilities[0]
    }


    for (i in 1 until utilities.size) {
        probs[i] = utilities[i] - utilities[i - 1]
    }
    return probs
}
/**
 * calculates the probabilities and also immediately cumulates them into the array, as compared to reduce them
 */
class MultinomialLogitArray : CumulateDistributionArray<Any?> {
    override fun tryCumulateProbabilities(utilities: DoubleArray, parameters: Any? ): Boolean {
        var infinityFlag = false
        var sum = .0
        for (i in utilities.indices) {
            val exp: Double = exp(utilities[i])
            utilities[i] = exp
            sum += exp
            if (exp == Double.POSITIVE_INFINITY) {
                infinityFlag = true
            }
        }
        // If there is at least one infinity, then we activate infinities as probability array
        if (infinityFlag) {
            activateInfinities(utilities)
            return true
        }

        var acc = .0 // Immediately track the increments to return the cumulated array instead of the distribution
        // array

        // If the sum is 0 then something went really wrong, and we can only use a uniform distribution
        if(sum == .0) {
            return false
//            for (i in utilities.indices) {
//                val probability: Double = 1.0 / utilities.size
//                utilities[i] = probability + acc
//                acc += probability
//            }
//            return
        }

        for (i in utilities.indices) {
            val probability: Double = utilities[i] / sum
            utilities[i] = probability + acc
            acc += probability
        }
        return true
    }

    /**
     * If some values are infinite, then the probability is simply uniformly distributed between these, and all others
     * are 0.0
     */
    private fun activateInfinities(array: DoubleArray) {
        val infinities = array.count { it == Double.POSITIVE_INFINITY }
        val probability = 1.0 / infinities
        var acc = .0
        for (i in array.indices) {
            val newValue: Double = if (array[i] == Double.POSITIVE_INFINITY) probability + acc else 0.0
            acc += newValue
            array[i] = newValue
        }
    }
}