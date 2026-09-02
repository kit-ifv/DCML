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
fun interface FloatCumulateDistributionArray<in P> {
    /**
     * @return `true` if a valid cumulative distribution was written into the given [utilities] array. Returns `false` if the
     * utilities cannot produce positive finite probability mass, for example because all relevant
     * values underflow to zero. In that case, the caller is expected to use a fallback strategy.
     */
    fun tryCumulateProbabilities(utilities: FloatArray, parameters: P): Boolean
}