package edu.kit.ifv.mobitopp.discretechoice.distribution

/**
 * Some distribution functions do not use the parameter object when calculating the utilityassignment (namely [MultinomialLogit])
 */
fun interface ParameterlessDistribution {
    fun calculateProbabilities(utilities: DoubleArray): DoubleArray
}