package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random

data class RandomChoiceModel<A, C>(override val name: String, override val choices: Set<A>) :
    FixedChoiceModel<A, C> {


    /**
     * @param random some random generator.
     * @param choices the set of alternatives one is chosen from.
     * @return one chosen alternative
     */
    context(_: C, random: Random)
    override fun select(choices: Set<A>): A {
        return choices.random(random)
    }

    context(_: C, random: Random)
    override fun select(): A {
        return choices.random(random)
    }

    context(_: C)
    override fun utility(alternative: A): Double {
        return -0.0
    }

    override fun probabilities(utilities: Map<A, Double>): Map<A, Double> {
        return choices.associateWith { 1.0 / choices.size }
    }

    context(_: C, random: Random)
    override fun selectInjected(
        choices: Set<A>,
        injections: Map<A, (Double) -> Double>,
    ): A {
        TODO("Not yet implemented")
    }

}