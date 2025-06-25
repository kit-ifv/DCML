package discreteChoice.distribution

/**
 * Some distribution functions do not use the parameter object when calculating the utility (namely [MultinomialLogit])
 */
fun interface ParameterlessDistribution<X> {
    fun calculateProbabilities(utilities: Map<X, Double>): Map<X, Double>
}