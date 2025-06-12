package discreteChoice.distribution

import discreteChoice.DistributionFunction
import kotlin.math.exp

/**
 * Classic soft-max approach to calculating probabilities out of utility-values.
 * Probability of option p_i := e^u_i / {sum of all utilities  e^u_k}
 * TODO test what happens when u_i > 200 or something so when e^u_i is practicly infinity.
 * Or define how it's supposed to work.
 */
class MultinomialLogit<X, P> : DistributionFunction<X, P> {

    override fun calculateProbabilities(utilities: Map<X, Double>, parameters: P): Map<X, Double> {
        val currentExp = utilities.entries.associate {
            it.key to exp(it.value)
        }
        val sum = currentExp.values.sum()

        return currentExp.mapValues { it.value / sum }
    }
}
