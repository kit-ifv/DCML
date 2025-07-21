package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random

data class RandomChoiceModel<A, C>(override val name: String, val choices: Set<A>) :
    BasicChoiceModel<A, C> {


    /**
     * @param random some random generator.
     * @param choices the set of alternatives one is chosen from.
     * @return one chosen alternative
     */
    context(_: C, random: Random)
    override fun select(choices: Set<A>): A {
        return choices.random(random)
    }

}