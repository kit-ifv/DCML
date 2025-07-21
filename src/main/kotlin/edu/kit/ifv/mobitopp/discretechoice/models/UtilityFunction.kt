package edu.kit.ifv.mobitopp.discretechoice.models

/**
 * A utilityassignment function takes in an alternative and a parameter object and returns the utilityassignment of said alternative.
 */
fun interface UtilityFunction<A, C, P> {
    fun calculateUtility(alternative: A, characteristics: C, parameterObject: P): Double
}