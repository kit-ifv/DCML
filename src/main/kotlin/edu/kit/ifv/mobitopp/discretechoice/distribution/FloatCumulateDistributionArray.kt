package edu.kit.ifv.mobitopp.discretechoice.distribution

fun interface FloatCumulateDistributionArray<in P> {
    fun tryCumulateProbabilities(utilities: FloatArray, parameters: P): Boolean
}