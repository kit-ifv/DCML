package edu.kit.ifv.mobitopp.discretechoice.models

import edu.kit.ifv.mobitopp.discretechoice.distribution.CumulateDistributionArray
import edu.kit.ifv.mobitopp.discretechoice.distribution.MultinomialLogitArray
import edu.kit.ifv.mobitopp.discretechoice.distribution.probabilities
import edu.kit.ifv.mobitopp.discretechoice.selection.SelectionFunctionArray
import edu.kit.ifv.mobitopp.discretechoice.selection.WeightedSelection
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.UtilityEnumeration
import kotlin.random.Random

/**
 * This class uses, and in particular reuses a double array for both the utilities and the probabilities.
 * The array is modified inplace, using the corresponding CumulateDistribution Array and SelectionFunctionArray.
 *
 *
 */

class ArrayBackedFixedChoiceModel<A, C, P>(
    val utilityAssignment: UtilityEnumeration<A, C, P>,
    val distributionFunction: CumulateDistributionArray<P> = MultinomialLogitArray(),
    val selectionFunction: SelectionFunctionArray = WeightedSelection(),
    val parameters: P,
    override val name: String,
) : FixedChoiceModel<A, C> {
    override val choices: Set<A> = utilityAssignment.options


    private val alternatives: List<A> = choices.toList()
    private val lookup: Map<A, Int> = alternatives.withIndex().associate { it.value to it.index }

    fun getIndex(alternative: A): Int {
        return lookup[alternative] ?: error("Alternative $alternative is not found in lookup")
    }

    private val utilityFunctions: Array<UtilityFunction<A, C, P>> = alternatives.map {
        utilityAssignment.getUtilityFunctionFor(it)!!

    }.toTypedArray()

    context(c: C, random: Random)
    override fun select(): A {
        val calculationArray = DoubleArray(choices.size) {
            utilityFunctions[it].calculateUtility(alternatives[it], c, parameters)
        }
        return selectInternal(calculationArray)

    }

    context(c: C, random: Random)
    override  fun selectFiltered(filter: (A) -> Boolean): A {

        val calculationArray = DoubleArray(choices.size) {
            if (filter(alternatives[it])) {
                utilityFunctions[it].calculateUtility(
                    alternatives[it], c,
                    parameters
                )
            } else {
                Double.NEGATIVE_INFINITY
            }

        }
        return selectInternal(calculationArray)
    }

    context(random: Random)
    private fun selectInternal(array: DoubleArray): A {
        distributionFunction.cumulateProbabilities(array, parameters)
        val outputIdx = selectionFunction.calculateSelection(
            array, random
        )
        return alternatives[outputIdx]
    }

    /**
     * @param random some random generator.
     * @param choices the set of alternatives one is chosen from.
     * @return one chosen alternative
     */
    context(c: C, random: Random)
    override fun select(choices: Set<A>): A {

        val calculationArray = DoubleArray(alternatives.size) {
            Double.NEGATIVE_INFINITY
        }
        choices.forEach {
            val idx = getIndex(it)
            calculationArray[idx] = utilityFunctions[idx].calculateUtility(it, c, parameters)
        }
        return selectInternal(calculationArray)
    }

    override fun probabilities(utilities: Map<A, Double>): Map<A, Double> {
        val keyMap = utilities.mapKeys { getIndex(it.key) }
        val calculationArray = DoubleArray(alternatives.size) {

            keyMap[it] ?: Double.NEGATIVE_INFINITY
        }
        distributionFunction.probabilities(calculationArray, parameters)
        return utilities.keys.associateWith {
            calculationArray[getIndex(it)]
        }
    }

    context(c: C)
    override fun utility(alternative: A): Double {
        return utilityFunctions[getIndex(alternative)].calculateUtility(alternative, c, parameters)
    }

    context(c: C, random: Random)
    override fun selectInjected(
        choices: Set<A>,
        injections: Map<A, (Double) -> Double>,
    ): A {
        val calculationArray = DoubleArray(alternatives.size) {

            Double.NEGATIVE_INFINITY
        }
        choices.forEach {
            val idx = getIndex(it)
            calculationArray[idx] = utilityFunctions[idx].calculateUtility(it, c, parameters)
        }
        injections.forEach { (key, operation) ->
            val idx = getIndex(key)
            calculationArray[idx] = operation(calculationArray[idx])

        }
        return selectInternal(calculationArray)
    }


}