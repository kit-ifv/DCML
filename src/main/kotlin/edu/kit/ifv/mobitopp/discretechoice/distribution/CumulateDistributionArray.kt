package edu.kit.ifv.mobitopp.discretechoice.distribution

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