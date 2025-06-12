package discreteChoice.distribution

import discreteChoice.DistributionFunction
import kotlin.math.exp

/**
 * Classic soft-max approach to calculating probabilities out of utility-values.
 * Probability of option p_i := e^u_i / {sum of all utilities  e^u_k}
 *
 * __Edge cases:__
 *
 * If utilities greater than 710 exist, each option with a utility >= 710 gets an equal probability, all others get 0. (
 * because `exp(710) == Double.POSITIVE_INFINITY`)
 *
 * If utilities are all  <=-745 each option gets the same probability. (Because exp(-745) == 0.0 leading division with
 * zero)
 */
class MultinomialLogit<X, P> : DistributionFunction<X, P> {

    override fun calculateProbabilities(utilities: Map<X, Double>, parameters: P): Map<X, Double> {
        val currentExp = utilities.entries.associate {
            it.key to exp(it.value)
        }
        val countInfinity = currentExp.count { it.value == Double.POSITIVE_INFINITY }
        if (countInfinity > 0) {
            val equalPos = currentExp.mapValues {
                if (it.value == Double.POSITIVE_INFINITY) 1.0 / countInfinity else 0.0
            }
            return equalPos
        }
        val sum = currentExp.values.sum()

        if(sum == 0.0) {
            return utilities.mapValues { 1.0 / utilities.size }
        }
        return currentExp.mapValues { it.value / sum }
    }
}
