package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random

data class FixedChoiceModel<A, C>(
    val original: UtilityBasedChoiceModel<A, C>,
    val choices: Set<A>,
): UtilityBasedChoiceModel<A, C> by original {

    context(_: C, random: Random)
    fun select(): A = original.select(choices)
    override val name: String = original.name

    /**
     * @param random some random generator.
     * @param choices the set of alternatives one is chosen from.
     * @return one chosen alternative
     */
    context(_: C, _: Random)
    override fun select(choices: Set<A>): A {
        return original.select(choices)
    }
    context(_: C) fun probabilities() = original.probabilities(choices)
    context(_: C) fun utilities() = original.utilities(choices)

    override fun addFilter(filter: ChoiceFilter<A, C>) = FilteredFixedChoiceModel<A, C>(original, choices, filter)
}