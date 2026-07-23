package edu.kit.ifv.mobitopp.discretechoice.selection

class FloatWeightedBinarySelection : FloatSelectionFunctionArray {

    override fun pick(
        probabilities: FloatArray,
        random: Double,
    ): Int {
        var low = 0
        var high = probabilities.size

        while (low < high) {
            val middle = low + (high - low) / 2

            if (probabilities[middle] >= random) {
                high = middle
            } else {
                low = middle + 1
            }
        }

        return low
    }
}