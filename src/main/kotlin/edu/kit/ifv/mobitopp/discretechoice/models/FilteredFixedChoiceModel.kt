package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random


class FilteredFixedChoiceModel<A, in C>(
    original: UtilityBasedChoiceModel<A, C>,
    override val choices: Set<A>,
    filter: ChoiceFilter<A, C>,


    ) : FilteredChoiceModel<A, C>(original, filter), UtilityBasedChoiceModel<A, C>, FixedChoiceModel<A, C> {
    override val name: String = original.name

    context(_: C, random: Random)
    override fun select(): A {
        return original.select(filtered())
    }

    context(_: C)
    private fun filtered() = choices.filter { filter.filter(it) }
    context(_: C) override fun probabilities() = original.probabilities(filtered())
    context(_: C) override fun utilities() = original.utilities(filtered())


    context(_: C) override fun utilities(vararg alternative: A) = original.utilities(alternative.filter { filter.filter(it) })

}

fun <A, C> FilteredFixedChoiceModel<A, C>.addFilter(filter: ChoiceFilter<A, C>): FilteredFixedChoiceModel<A, C> {
    return FilteredFixedChoiceModel(filter = this.filter + filter, choices = choices, original = original)
}