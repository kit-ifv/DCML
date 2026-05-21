package edu.kit.ifv.mobitopp.discretechoice.selection

import edu.kit.ifv.mobitopp.discretechoice.utils.cumulativeSum
import edu.kit.ifv.mobitopp.discretechoice.utils.toIndex
import kotlin.random.Random

/**
 * Works index based
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

fun <K> Map<K, Double>.select(random: Double): K {
    require(isNotEmpty()) {
        "Cannot pick a value from an empty map"
    }
    require(random in 0.0..1.0) {
        "Need a random value between 0 and 1"
    }
    val target = cumulativeSum()
    val ins = target.binarySearch { it.first.compareTo(random * target.last().first) }
    val choice = target[ins.toIndex()].second
    return choice
}