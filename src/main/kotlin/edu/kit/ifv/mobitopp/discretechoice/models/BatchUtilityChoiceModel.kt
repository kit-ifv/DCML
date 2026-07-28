package edu.kit.ifv.mobitopp.discretechoice.models

import edu.kit.ifv.mobitopp.discretechoice.distribution.CumulateDistributionArray
import edu.kit.ifv.mobitopp.discretechoice.distribution.FloatCumulateDistributionArray
import edu.kit.ifv.mobitopp.discretechoice.selection.FloatSelectionFunctionArray
import edu.kit.ifv.mobitopp.discretechoice.selection.SelectionFunctionArray
import kotlin.random.Random

/**
 * This is a choice model, that is fixed, so it always selects out of the same set of alternatives. It also works on
 * indices, so the returned alternative is an integer in 0 inclusive [size] exclusive.
 */
interface BatchUtilityChoiceModel<C, P>: TrulyFixedChoiceModel<Int, C> {
    val parameters: P
    val size: Int
    val distributionFunction: CumulateDistributionArray<Any?>
    val selectionFunction: SelectionFunctionArray

    override val choices: Set<Int>
        get() = (0..<size).toSet()

    /**
     * Generates all utilities for a situation in one go. The array probabilities have to have the size of [size].
     */
    context(characteristic: C)
    fun P.generateUtilitiesArray(): DoubleArray


    context(characteristic: C, random: Random)
    override fun select(): Int {
        val array = parameters.generateUtilitiesArray()
        val success = distributionFunction.tryCumulateProbabilities(array, null)
        if (!success) {
            throw IllegalStateException("Failed to cumulate probabilities, which should be impossible")
        }
        return selectionFunction.calculateSelection(array, random)
    }
}

/**
 * This is a choice model, that is fixed, so it always selects out of the same set of alternatives. It also works on
 * indices, so the returned alternative is an integer in 0 inclusive [size] exclusive. This works on float arrays
 * internally.
 */
interface FloatBatchUtilityChoiceModel<C, P>: TrulyFixedChoiceModel<Int, C> {
    val parameters: P
    val size: Int
    val distributionFunction: FloatCumulateDistributionArray<Any?>
    val selectionFunction: FloatSelectionFunctionArray

    override val choices: Set<Int>
        get() = (0..<size).toSet()

    /**
     * Generates all utilities for a situation in one go. The array probabilities have to have the size of [size].
     */
    context(characteristic: C)
    fun P.generateUtilitiesArray(): FloatArray


    context(characteristic: C, random: Random)
    override fun select(): Int {
        val array = parameters.generateUtilitiesArray()
        val success = distributionFunction.tryCumulateProbabilities(array, null)
        if (!success) {
            throw IllegalStateException("Failed to cumulate probabilities, which should be impossible")
        }
        return selectionFunction.calculateSelection(array, random)
    }
}