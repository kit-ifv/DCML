package edu.kit.ifv.mobitopp.discretechoice.distribution

import kotlin.math.exp


/**
 * A Distribution Function using a soft-max / normalized exponential function approach to calculating probabilities out
 * of utilityassignment-values. Probability of option p_i := e^u_i / {sum of all utilities  e^u_k}
 *
 * __To have no unexpected behaviour, ensure that none of the utilityassignment values are greater than 710 and not all are
 * smaller than -744.__
 *
 * __Edge cases:__
 *
 * If utilities greater than 710 exist, each option with a utilityassignment >= 710 gets an equal probability, all others get 0. (
 * because `exp(710) == Double.POSITIVE_INFINITY`)
 *
 * If utilities are all  <=-745 each option gets the same probability. (Because exp(-745) == 0.0 leading division with
 * zero)
 */
class MultinomialLogit<A, P> : DistributionFunction<P>, ParameterlessDistribution {

    override fun calculateProbabilities(utilities: DoubleArray, parameters: P): DoubleArray {
        return calculateProbabilities(utilities)
    }


    private fun Map<A, Double>.containsInfinity(): Boolean {
        return any { it.value == Double.POSITIVE_INFINITY }
    }

    private fun Map<A, Double>.mapInfinityToEqualDistribution(): Map<A, Double> {
        val numberOfInfinity = count { it.value == Double.POSITIVE_INFINITY }
        val equalProb = 1.0 / numberOfInfinity
        return this.mapValues { if (it.value == Double.POSITIVE_INFINITY) equalProb else 0.0 }
    }

    override fun calculateProbabilities(utilities: DoubleArray): DoubleArray {
        utilities.forEachIndexed  { a,b ->
            utilities[a] = exp(b)
        }
        val sum = utilities.sum()

        if (sum == 0.0) {
            utilities.forEachIndexed  { i, _ ->
                utilities[i] = (1/ utilities.size).toDouble()
            }
            return utilities
        }
        utilities.forEachIndexed  { i, v ->
            utilities[i] = v / sum
        }
        return utilities
    }
}
