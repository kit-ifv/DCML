package edu.kit.ifv.mobitopp.discretechoice.selection

class FloatWeightedSelection: FloatSelectionFunctionArray {
    override fun pick(probabilities: FloatArray, random: Double): Int {
        for(i in probabilities.indices){
            if(probabilities[i] >= random){
                return i
            }
        }
        return probabilities.size
    }
}