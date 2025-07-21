package edu.kit.ifv.mobitopp.discretechoice.distribution

/**
 * A distribution function takes in a map, which contains each choosable object(alternative) with their associated
 * utilityassignment functions already calculated. Also takes a parameter object and returns a map of calculated probabilities
 * from the given alternatives.
 */
fun interface DistributionFunction<A, P> {
    fun calculateProbabilities(utilities: Map<A, Double>, parameters: P): Map<A, Double>
}