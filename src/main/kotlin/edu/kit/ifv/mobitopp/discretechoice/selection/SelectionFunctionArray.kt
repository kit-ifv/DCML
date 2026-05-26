package edu.kit.ifv.mobitopp.discretechoice.selection

import edu.kit.ifv.mobitopp.discretechoice.utils.cumulativeSum
import edu.kit.ifv.mobitopp.discretechoice.utils.toIndex
import kotlin.random.Random

/**
 * Works index based, returns the index of the selected probability.
 */
fun interface SelectionFunctionArray{
    fun calculateSelection(probabilities: DoubleArray, random: Random, ): Int = pick(probabilities, random.nextDouble())
    fun pick(probabilities: DoubleArray, random: Double): Int
}

/**
 * Assume that probabilities is a cumulated array, otherwise this will not work.
 */
class WeightedSelection: SelectionFunctionArray {
    override fun pick(probabilities: DoubleArray, random: Double): Int {
        for(i in probabilities.indices){
            if(probabilities[i] >= random){
                return i
            }
        }
        return probabilities.size
    }
}

