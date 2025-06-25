package discreteChoice.distribution

import discreteChoice.DistributionFunction
import kotlin.math.exp

/**
 * A Distribution Function using a soft-max / normalized exponential function approach to calculating probabilities out
 * of utility-values. Probability of option p_i := e^u_i / {sum of all utilities  e^u_k}
 *
 * __To have no unexpected behaviour, ensure that none of the utility values are greater than 710 and not all are
 * smaller than -744.__
 *
 * __Edge cases:__
 *
 * If utilities greater than 710 exist, each option with a utility >= 710 gets an equal probability, all others get 0. (
 * because `exp(710) == Double.POSITIVE_INFINITY`)
 *
 * If utilities are all  <=-745 each option gets the same probability. (Because exp(-745) == 0.0 leading division with
 * zero)
 */
class MultinomialLogit<X, P> : DistributionFunction<X, P>, ParameterlessDistribution<X> {

    override fun calculateProbabilities(utilities: Map<X, Double>, parameters: P): Map<X, Double> {
        return calculateProbabilities(utilities)
    }



    private fun Map<X, Double>.containsInfinity(): Boolean {
        return any { it.value == Double.POSITIVE_INFINITY }
    }

    private fun Map<X, Double>.mapInfinityToEqualDistribution(): Map<X, Double> {
        val numberOfInfinity = count { it.value == Double.POSITIVE_INFINITY }
        val equalProb = 1.0 / numberOfInfinity
        return this.mapValues { if (it.value == Double.POSITIVE_INFINITY) equalProb  else 0.0 }
    }

    override fun calculateProbabilities(utilities: Map<X, Double>): Map<X, Double> {
        val expUtilities = utilities.entries.associate {
            it.key to exp(it.value)
        }
        if (expUtilities.containsInfinity()) {
            return expUtilities.mapInfinityToEqualDistribution()
        }
        val sum = expUtilities.values.sum()

        if(sum == 0.0) {
            return utilities.mapValues { 1.0 / utilities.size }
        }
        return expUtilities.mapValues { it.value / sum }
    }
}
