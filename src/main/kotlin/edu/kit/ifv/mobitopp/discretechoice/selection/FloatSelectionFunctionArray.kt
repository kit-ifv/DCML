package edu.kit.ifv.mobitopp.discretechoice.selection

import kotlin.random.Random

/**
 * Functional interface for a function that selects an element from an array of cumulated probabilities and a given
 * random number in `[0,1)`.
 */
fun interface FloatSelectionFunctionArray{
    /**
     * Selects one of the entries, returns the index.
     * Probabilities need to be cumulated.
     * @param probabilities a sorted list of floats with the last float being `1f`. All entries need to be in `[0f,1f]`.
     * @return a valid index of [probabilities] that represents the selected element.
     */
    fun calculateSelection(probabilities: FloatArray, random: Random): Int = pick(probabilities, random.nextDouble())
    fun pick(probabilities: FloatArray, random: Double): Int
}