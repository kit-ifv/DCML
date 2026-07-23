package edu.kit.ifv.mobitopp.discretechoice.selection

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