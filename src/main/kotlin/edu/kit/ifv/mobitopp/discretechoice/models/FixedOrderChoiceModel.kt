package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random

data class FixedOrderChoiceModel<A, C>(
    override val name: String,
    val choices: Set<A>,
    val filter: ChoiceFilter<A, C>,
) : BasicChoiceModel<A, C> {
    /**
     * @param random some random generator.
     * @param choices the set of alternatives one is chosen from.
     * @return one chosen alternative
     */
    context(_: C, _: Random)
    override fun select(choices: Set<A>): A {
        val filtered = filter.filter(choices)
        return this.choices.first { it in filtered }
    }
}