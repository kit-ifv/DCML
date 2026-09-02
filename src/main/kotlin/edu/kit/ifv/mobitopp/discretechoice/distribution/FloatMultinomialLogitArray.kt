package edu.kit.ifv.mobitopp.discretechoice.distribution

import kotlin.math.exp

class FloatMultinomialLogitArray: FloatCumulateDistributionArray<Any?> {
    override fun tryCumulateProbabilities(utilities: FloatArray, parameters: Any?): Boolean {
        val max = utilities.max()
        var sum = .0f
        for (i in utilities.indices) {
            val exp: Float = exp(utilities[i] - max)
            utilities[i] = exp
            sum += exp
        }

        var acc = .0f // Immediately track the increments to return the cumulated array instead of the distribution
        // array

        // If the sum is 0 then something went really wrong, the caller should now handle this case.
        if(sum == .0f) {
            return false
        }

        for (i in utilities.indices) {
            val probability: Float = utilities[i] / sum
            utilities[i] = probability + acc
            acc += probability
        }

        return true
    }
}