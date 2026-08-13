package edu.kit.ifv.mobitopp.discretechoice.models

import edu.kit.ifv.mobitopp.discretechoice.distribution.CumulateDistributionArray
import edu.kit.ifv.mobitopp.discretechoice.distribution.FloatCumulateDistributionArray
import edu.kit.ifv.mobitopp.discretechoice.distribution.MultinomialLogitArray
import edu.kit.ifv.mobitopp.discretechoice.selection.FloatSelectionFunctionArray
import edu.kit.ifv.mobitopp.discretechoice.selection.SelectionFunctionArray
import edu.kit.ifv.mobitopp.discretechoice.selection.WeightedSelection
import kotlin.collections.toFloatArray
import kotlin.random.Random

/**
 * This is a choice model, that is fixed, so it always selects out of the same set of alternatives. Internally it
 * works on indices and arrays. this makes it efficient for selecting an alternative out of all alternatives.
 * This choicemodel is inefficient regarding calculating single utilities because it always calculates all utilities at
 * once with [generateUtilitiesArray].
 * @param parameters The model parameters used to compute utility values
 *                      (e.g., coefficients, weights, thresholds).
 * @param  distributionFunction The cumulative distribution function used to
 *                                convert utilities into an array of cumulated probabilities.
 *                                Defaults to [MultinomialLogitArray].
 * @param  selectionFunction The function that draws a random alternative
 *                             based on the computed cumulated probability distribution.
 *                             Defaults to [WeightedSelection].
 */
abstract class BatchUtilityChoiceModel<C, P, A>(
    val parameters: P,
    val distributionFunction: CumulateDistributionArray<Any?> = MultinomialLogitArray(),
    val selectionFunction: SelectionFunctionArray = WeightedSelection(),
): FixedChoiceModel<A, C> {
    private val alternativeToIndex: Map<A, Int> by lazy {
        choices.toList().withIndex().associate{ it.value to it.index }
    }
    private val indexToAlternative: Map<Int, A> by lazy {
        choices.toList().withIndex().associate{ it.index to it.value}
    }

    /**
     * Generates all utilities for a situation in one go. The array probabilities have to have the size of [choices.size].
     */
    context(characteristic: C)
    abstract fun P.generateUtilitiesArray(): DoubleArray


    context(characteristic: C, random: Random)
    override fun select(): A {
        val array = parameters.generateUtilitiesArray()
        val success = distributionFunction.tryCumulateProbabilities(array, null)
        if (!success) {
            throw IllegalStateException("'$name'-Model: Failed to cumulate probabilities, which should be impossible")
        }
        val selectedIndex = selectionFunction.calculateSelection(array, random)
        return indexToAlternative[selectedIndex]!!
    }

    /**
     * Only use this if you really need the utility for a single alternative, this is calculating all utilities internally.
     */
    context(_: C)
    override fun utility(alternative: A): Double {
        if (!this.choices.contains(alternative)) error("Model called with invalid alternative $alternative")
        val alternativeIndex = alternativeToIndex[alternative]!!
        return parameters.generateUtilitiesArray()[alternativeIndex]
    }

    override fun probabilities(utilities: Map<A, Double>): Map<A, Double> {
        if (!this.choices.containsAll(utilities.keys)) error("Model '$name' called with invalid indices.")
        val bufferArray = choices.map {
            utilities[it] ?: Double.NEGATIVE_INFINITY
        }.toTypedArray().toDoubleArray()
        if (!distributionFunction.tryCumulateProbabilities(bufferArray, parameters)) {
            error("Distribution function could not cumulate probabilities for the given array ${bufferArray.contentToString()}")
        }
        return utilities.mapValues { (alternative, _) ->
            val alternativeIndex = alternativeToIndex[alternative]!!
            val previousProb = bufferArray[alternativeIndex - 1] ?: 0.0
            val probCumulated = bufferArray[alternativeIndex]
            probCumulated - previousProb // de-cumulate probabilities
        }
    }

    context(_: C, random: Random)
    override fun selectInjected(
        choices: Set<A>,
        injections: Map<A, (Double) -> Double>
    ): A {
        if (!this.choices.containsAll(choices)) error("model '$name' selectInjected called with choices " +
                "outside of the models default choices. A BatchUtilityChoiceModel does not support this.")
        require(choices.containsAll(injections.keys)) { "Inconsistent parameters."}
        val utilities = parameters.generateUtilitiesArray()
        val filteredUtilities = DoubleArray(this.choices.size) { index ->
            val alternative = indexToAlternative[index]
            if (choices.contains(alternative)) {
                injections[alternative]?.invoke(utilities[index]) ?: utilities[index]
            } else Double.NEGATIVE_INFINITY
        }

        if (!distributionFunction.tryCumulateProbabilities(filteredUtilities, null)) {
            error("Could not cumulate probabilities. From the given utilities: ${filteredUtilities.joinToString(", ")}")
        }
        val selectedIndex = selectionFunction.calculateSelection(filteredUtilities, random)
        return indexToAlternative[selectedIndex]!!
    }

    context(_: C, random: Random)
    override fun select(choices: Set<A>): A {
        if (!this.choices.containsAll(choices)) error("model '$name' called with invalid indices.")
        val utilities = parameters.generateUtilitiesArray()
        val filteredUtilities = DoubleArray(this.choices.size) { index ->
            val alternative = indexToAlternative[index]
            if (choices.contains(alternative)) utilities[index] else Double.NEGATIVE_INFINITY
        }
        if (!distributionFunction.tryCumulateProbabilities(filteredUtilities, null)) {
            error("Could not cumulate probabilities. From the given utilities: ${filteredUtilities.joinToString(", ")}")
        }
        val selectedIndex = selectionFunction.calculateSelection(filteredUtilities, random)
        return indexToAlternative[selectedIndex]!!
    }
}

/**
 * This is like a [BatchUtilityChoiceModel] but it works on FloatArrays internally, instead of DoubleArray. Other than
 * that they are identical.
 * This is a choice model, that is fixed, so it always selects out of the same set of alternatives. Internally it
 * works on indices and arrays. this makes it efficient for selecting an alternative out of all alternatives.
 * This choicemodel is inefficient regarding calculating single utilities because it always calculates all utilities at
 * once with [generateUtilitiesArray].
 * @param parameters The model parameters used to compute utility values
 *                      (e.g., coefficients, weights, thresholds).
 * @param  distributionFunction The cumulative distribution function used to
 *                                convert utilities into an array of cumulated probabilities.
 *                                Defaults to [MultinomialLogitArray].
 * @param  selectionFunction The function that draws a random alternative
 *                             based on the computed cumulated probability distribution.
 *                             Defaults to [WeightedSelection].
 */
abstract class FloatBatchUtilityChoiceModel<C, P, A>(
    val parameters: P,
    val distributionFunction: FloatCumulateDistributionArray<Any?>,
    val selectionFunction: FloatSelectionFunctionArray,
): FixedChoiceModel<A, C> {

    private val alternatives = choices.toList()
    private val alternativeToIndex: Map<A, Int> = choices.withIndex().associate{ it.value to it.index }

    /**
     * Generates all utilities for a situation in one go. The array probabilities have to have the size of [choices.size].
     */
    context(characteristic: C)
    abstract fun P.generateUtilitiesArray(): FloatArray


    context(characteristic: C, random: Random)
    override fun select(): A {
        val array = parameters.generateUtilitiesArray()
        val success = distributionFunction.tryCumulateProbabilities(array, null)
        if (!success) {
            throw IllegalStateException("'$name'-Model: Failed to cumulate probabilities, which should be impossible")
        }
        val selectedIndex = selectionFunction.calculateSelection(array, random)
        return alternatives[selectedIndex]!!
    }

    /**
     * Only use this if you really need the utility for a single alternative, this is calculating all utilities internally.
     */
    context(_: C)
    override fun utility(alternative: A): Double {
        if (!this.choices.contains(alternative)) error("Model called with invalid alternative $alternative")
        val alternativeIndex = alternativeToIndex[alternative]!!
        return parameters.generateUtilitiesArray()[alternativeIndex].toDouble()
    }

    /**
     * These are non-cumulated probabilities for each alternative.
     */
    override fun probabilities(utilities: Map<A, Double>): Map<A, Double> {
        if (!this.choices.containsAll(utilities.keys)) error("Model '$name' called with invalid indices.")
        val bufferArray = choices.map {
            utilities[it]?.toFloat() ?: Float.NEGATIVE_INFINITY
        }.toTypedArray().toFloatArray()
        if (!distributionFunction.tryCumulateProbabilities(bufferArray, parameters)) {
            error("Distribution function could not cumulate probabilities for the given array ${bufferArray.contentToString()}")
        }
        return utilities.mapValues { (alternative, _) ->
            // probabilities are cumulated
            val alternativeIndex = alternativeToIndex[alternative]!!
            val previousVal = bufferArray[alternativeIndex - 1] ?: 0f
            val currentVal = bufferArray[alternativeIndex]
            (currentVal - previousVal).toDouble() // de-cumulate probabilities
        }
    }

    context(_: C, random: Random)
    override fun selectInjected(
        choices: Set<A>,
        injections: Map<A, (Double) -> Double>
    ): A {
        if (!this.choices.containsAll(choices)) error("model '$name' selectInjected called with choices " +
                "outside of the models default choices. A BatchUtilityChoiceModel does not support this.")
        require(choices.containsAll(injections.keys)) { "Inconsistent parameters."}
        val utilities = parameters.generateUtilitiesArray()
        val filteredUtilities = FloatArray(this.choices.size) { index ->
            val alternative = alternatives[index]
            if (choices.contains(alternative)) {
                val utility = utilities[index]
                injections[alternative]?.invoke(utility.toDouble())?.toFloat() ?: utility
            } else Float.NEGATIVE_INFINITY
        }

        if (!distributionFunction.tryCumulateProbabilities(filteredUtilities, null)) {
            error("Could not cumulate probabilities. From the given utilities: ${filteredUtilities.joinToString(", ")}")
        }
        val selectedIndex = selectionFunction.calculateSelection(filteredUtilities, random)
        return alternatives[selectedIndex]!!
    }

    context(_: C, random: Random)
    override fun select(choices: Set<A>): A {
        if (!this.choices.containsAll(choices)) error("model '$name' called with invalid alternatives. " +
                "A BatchUtilityChoiceModel does not selecting from choices that are beyond the alternatives it is built for.")
        val utilities = parameters.generateUtilitiesArray()
        val filteredUtilities = FloatArray(this.choices.size) { index ->
            val alternative = alternatives[index]
            val utility = utilities[index]
            if (choices.contains(alternative))  utility else Float.NEGATIVE_INFINITY
        }
        if (!distributionFunction.tryCumulateProbabilities(filteredUtilities, null)) {
            error("Could not cumulate probabilities. From the given utilities: ${filteredUtilities.joinToString(", ")}")
        }
        val selectedIndex = selectionFunction.calculateSelection(filteredUtilities, random)
        return alternatives[selectedIndex]!!
    }
}
