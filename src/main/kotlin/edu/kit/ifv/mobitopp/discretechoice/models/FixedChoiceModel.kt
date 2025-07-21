package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random

interface FixedChoiceModel<A, C>: UtilityBasedChoiceModel<A, C> {

    val choices: Set<A>
    context(_: C, random: Random)
    fun select(): A
    override fun addFilter(filter: ChoiceFilter<A, C>) = FilteredFixedChoiceModel(this, choices, filter)
    companion object {
        operator fun <A, C> invoke(     original: UtilityBasedChoiceModel<A, C>,
                       choices: Set<A>,): FixedChoiceModel<A, C> {
            return FixedChoiceModelImpl(original, choices)
        }
    }
    context(_: C) fun probabilities() = this.probabilities(choices)
    context(_: C) fun utilities() = this.utilities(choices)
}

data class FixedChoiceModelImpl<A, C>(
    val original: UtilityBasedChoiceModel<A, C>,
    override val choices: Set<A>,
): UtilityBasedChoiceModel<A, C> by original, FixedChoiceModel<A, C> {
    context(_: C, random: Random)
    override fun select(): A = original.select(choices)
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


    override fun addFilter(filter: ChoiceFilter<A, C>) = FilteredFixedChoiceModel<A, C>(original, choices, filter)
}