package discreteChoice.distribution

import discreteChoice.DistributionFunction
import kotlin.math.exp

class MultinomialLogit<X, P> : DistributionFunction<X, P> {

    override fun calculateProbabilities(utilities: Map<X, Double>, parameters: P): Map<X, Double> {
        val currentExp = utilities.entries.associate {
            it.key to exp(it.value)
        }
        val sum = currentExp.values.sum()

        return currentExp.mapValues { it.value / sum }
    }
}
