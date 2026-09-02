package edu.kit.ifv.mobitopp.discretechoice.selection

import kotlin.random.Random

/**
 * Works index based, returns the index of the selected probability.
 */
fun interface SelectionFunctionArray{
    fun calculateSelection(probabilities: DoubleArray, random: Random, ): Int = pick(probabilities, random.nextDouble())
    fun pick(probabilities: DoubleArray, random: Double): Int
}


