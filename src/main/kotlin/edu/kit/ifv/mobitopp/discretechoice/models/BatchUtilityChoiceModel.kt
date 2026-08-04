package edu.kit.ifv.mobitopp.discretechoice.models

import edu.kit.ifv.mobitopp.discretechoice.distribution.CumulateDistributionArray
import edu.kit.ifv.mobitopp.discretechoice.distribution.FloatCumulateDistributionArray
import edu.kit.ifv.mobitopp.discretechoice.selection.FloatSelectionFunctionArray
import edu.kit.ifv.mobitopp.discretechoice.selection.SelectionFunctionArray
import kotlin.collections.toFloatArray
import kotlin.random.Random

/**
 * This is a choice model, that is fixed, so it always selects out of the same set of alternatives. It also works on
 * indices, so the returned alternative is an integer in 0 inclusive [size] exclusive.
 */
interface BatchUtilityChoiceModel<C, P>: FixedChoiceModel<Int, C> {
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
            throw IllegalStateException("'$name'-Model: Failed to cumulate probabilities, which should be impossible")
        }
        return selectionFunction.calculateSelection(array, random)
    }

    /**
     * Only use this if you really need the utility for a single alternative, this is calculating all utilities internally.
     */
    context(_: C)
    override fun utility(alternative: Int): Double {
        if (!this.choices.contains(alternative)) error("Model called with invalid index.")
        return parameters.generateUtilitiesArray()[alternative]
    }

    override fun probabilities(utilities: Map<Int, Double>): Map<Int, Double> {
        val requestedIndices = utilities.keys
        if (!this.choices.containsAll(requestedIndices)) error("Model '$name' called with invalid indices.")
        val bufferArray = choices.map {
            utilities[it] ?: Double.NEGATIVE_INFINITY
        }.toTypedArray().toDoubleArray()
        if (!distributionFunction.tryCumulateProbabilities(bufferArray, parameters)) {
            error("Distribution function could not cumulate probabilities for the given array $bufferArray")
        }
        return utilities.mapValues { (index, _) -> bufferArray[index] }
    }

    context(_: C, random: Random)
    override fun selectInjected(
        choices: Set<Int>,
        injections: Map<Int, (Double) -> Double>
    ): Int {
        if (!this.choices.containsAll(choices)) error("model '$name' called with invalid indices.")
        require(choices.containsAll(injections.keys)) { "Inconsistent parameters."}
        val utilities = parameters.generateUtilitiesArray()
        val filteredUtilities = DoubleArray(this.choices.size) { index ->
            if (choices.contains(index)) {
                injections[index]?.invoke(utilities[index]) ?: utilities[index]
            } else Double.NEGATIVE_INFINITY
        }

        if (!distributionFunction.tryCumulateProbabilities(filteredUtilities, null)) {
            error("Could not cumulate probabilities. From the given utilities: ${filteredUtilities.joinToString(", ")}")
        }
        return selectionFunction.calculateSelection(filteredUtilities, random)
    }

    context(_: C, random: Random)
    override fun select(choices: Set<Int>): Int {
        if (!this.choices.containsAll(choices)) error("model '$name' called with invalid indices.")
        val utilities = parameters.generateUtilitiesArray()
        val filteredUtilities = DoubleArray(this.choices.size) { index ->
            if (choices.contains(index)) utilities[index] else Double.NEGATIVE_INFINITY
        }
        if (!distributionFunction.tryCumulateProbabilities(filteredUtilities, null)) {
            error("Could not cumulate probabilities. From the given utilities: ${filteredUtilities.joinToString(", ")}")
        }
        return selectionFunction.calculateSelection(filteredUtilities, random)
    }
}

/**
 * This is a choice model, that is fixed, so it always selects out of the same set of alternatives. It also works on
 * indices, so the returned alternative is an integer in 0 inclusive [size] exclusive. This works on float arrays
 * internally.
 */
interface FloatBatchUtilityChoiceModel<C, P>: FixedChoiceModel<Int, C> {
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

    /**
     * Only use this if you really need the utility for a single alternative, this is calculating all utilities internally.
     */
    context(_: C)
    override fun utility(alternative: Int): Double {
        if (!this.choices.contains(alternative)) error("Model called with invalid index.")
        val util = parameters.generateUtilitiesArray()[alternative]
        if (util == Float.NEGATIVE_INFINITY) return Double.NEGATIVE_INFINITY
        else return util.toDouble()
    }

    override fun probabilities(utilities: Map<Int, Double>): Map<Int, Double> {
        val requestedIndices = utilities.keys
        if (!this.choices.containsAll(requestedIndices)) error("Model '$name' called with invalid indices.")
        val bufferArray = choices.map {
            utilities[it]?.toFloat() ?: Float.NEGATIVE_INFINITY
        }.toTypedArray().toFloatArray()
        if (!distributionFunction.tryCumulateProbabilities(bufferArray, parameters)) {
            error("Distribution function could not cumulate probabilities for the given array $bufferArray")
        }
        return utilities.mapValues { (index, _) -> bufferArray[index].toDouble() }
    }

    context(_: C, random: Random)
    override fun selectInjected(
        choices: Set<Int>,
        injections: Map<Int, (Double) -> Double>
    ): Int {
        if (!this.choices.containsAll(choices)) error("model '$name' called with invalid indices.")
        require(choices.containsAll(injections.keys)) { "Inconsistent parameters."}
        val utilities = parameters.generateUtilitiesArray()
        val filteredUtilities = FloatArray(this.choices.size) { index ->
            if (choices.contains(index)) {
                val util = utilities[index].toDouble()
                injections[index]?.invoke(util)?.toFloat() ?: util.toFloat()
            } else Float.NEGATIVE_INFINITY
        }

        if (!distributionFunction.tryCumulateProbabilities(filteredUtilities, null)) {
            error("Could not cumulate probabilities. From the given utilities: ${filteredUtilities.joinToString(", ")}")
        }
        return selectionFunction.calculateSelection(filteredUtilities, random)
    }

    context(_: C, random: Random)
    override fun select(choices: Set<Int>): Int {
        if (!this.choices.containsAll(choices)) error("model '$name' called with invalid indices.")
        val utilities = parameters.generateUtilitiesArray()
        val filteredUtilities = FloatArray(this.choices.size) { index ->
            if (choices.contains(index)) utilities[index] else Float.NEGATIVE_INFINITY
        }
        if (!distributionFunction.tryCumulateProbabilities(filteredUtilities, null)) {
            error("Could not cumulate probabilities. From the given utilities: ${filteredUtilities.joinToString(", ")}")
        }
        return selectionFunction.calculateSelection(filteredUtilities, random)
    }
}