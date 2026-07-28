package edu.kit.ifv.mobitopp.discretechoice.models

import edu.kit.ifv.mobitopp.discretechoice.distribution.FloatMultinomialLogitArray
import edu.kit.ifv.mobitopp.discretechoice.distribution.MultinomialLogitArray
import edu.kit.ifv.mobitopp.discretechoice.selection.FloatWeightedBinarySelection
import edu.kit.ifv.mobitopp.discretechoice.selection.WeightedSelection
import kotlin.random.Random


abstract class CompiledChoiceModelDouble<C, P>: FixedChoiceModel<Int, C> {
    protected val distributionFunction = MultinomialLogitArray()
    protected val selectionFunction = WeightedSelection()


    /**
     * @param random some random generator.
     * @param choices the set of alternatives one is chosen from.
     * @return one chosen alternative
     */
    context(_: C, _: Random)
    override fun select(choices: Set<Int>): Int {
        TODO("Not yet implemented")
    }

    override fun probabilities(utilities: Map<Int, Double>): Map<Int, Double> {
        TODO("Not yet implemented")
    }

    context(_: C)
    override fun utility(alternative: Int): Double {
        TODO("Not yet implemented")
    }

    context(_: C, random: Random)
    override fun selectInjected(
        choices: Set<Int>,
        injections: Map<Int, (Double) -> Double>,
    ): Int {
        TODO("Not yet implemented")
    }


    override val name: String
        get() = TODO("Not yet implemented")
}



abstract class CompiledChoiceModel<C, P>: TrulyFixedChoiceModel<Int, C> {
    protected val alternatives: IntArray = IntArray(4)

    override val choices: Set<Int> = alternatives.toSet()
    protected val distributionFunction = FloatMultinomialLogitArray()
    protected val selectionFunction = FloatWeightedBinarySelection()
    context(random: Random)
    protected inline fun selectInternal(array: FloatArray, fallbackOptions: () -> Collection<Int> = { choices }): Int {
        val success = distributionFunction.tryCumulateProbabilities(array, null)
        if (!success) {
            return fallbackOptions().random(random)
        }
        val outputIdx = selectionFunction.calculateSelection(
            array, random
        )
        return alternatives[outputIdx]
    }
}