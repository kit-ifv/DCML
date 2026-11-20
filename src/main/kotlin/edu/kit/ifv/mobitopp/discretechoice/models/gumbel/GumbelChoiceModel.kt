package edu.kit.ifv.mobitopp.discretechoice.models.gumbel

import edu.kit.ifv.mobitopp.discretechoice.models.UtilityBasedChoiceModel
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.UtilityAssignment
import kotlin.math.ln
import kotlin.random.Random

fun interface EpsilonSampler<in A> {
    // Adds or subtracts the epsilons from the target utility array
    fun sampleOnto(
        choices: List<A>,
        random: Random,
        destination: DoubleArray
    )
}
class IndependentGumbel: EpsilonSampler<Any?> {
    override fun sampleOnto(
        choices: List<Any?>,
        random: Random,
        destination: DoubleArray,
    ) {
        destination.indices.forEach {
            destination[it] += -ln(-ln(random.nextDouble()))
        }
    }
}

class GumbelChoiceModel<A,C ,P>(
    private val epsilonSampler: EpsilonSampler<A>,
    val parameters: P,
    private val utilityAssignment: UtilityAssignment<A, C, P>,
    override val name: String
): UtilityBasedChoiceModel<A, C> {
    context(_: C)
    override fun utility(alternative: A): Double {
        TODO("Not yet implemented")
    }


    override fun probabilities(utilities: Map<A, Double>): Map<A, Double> {
        TODO("Not yet implemented")
    }
    context(c: C, random: Random)
    fun selectE(choices: List<A>): A {

        val calculationArray = DoubleArray(choices.size) {
            utilityAssignment.getUtilityFunctionFor(choices[it])?.calculateUtility(choices[it], c, parameters)
                ?: Double.NEGATIVE_INFINITY
        }
        epsilonSampler.sampleOnto(choices, random, calculationArray)

        var maxIndex = 0
        var maxValue = calculationArray[0]

        for (i in 1 until calculationArray.size) {
            val value = calculationArray[i]
            if (value > maxValue) {
                maxValue = value
                maxIndex = i
            }
        }
        return choices[maxIndex]
    }

    context(c: C, random: Random)
    override fun select(choices: List<A>): A {
        return selectE(choices)
    }
    /**
     * @param injections a map, which maps the generated utilities for each alternative to some other utility.
     */
    context(_: C, random: Random)
    override fun selectInjected(
        choices: List<A>,
        injections: Map<A, (Double) -> Double>,
    ): A {
        TODO("Not yet implemented")
    }







}