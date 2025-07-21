package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random




class FilteredFixedChoiceModel<A, C>(
    original: UtilityBasedChoiceModel<A, C>,
    val choices: Set<A>,
    filter: ChoiceFilter<A, C>,



    ): FilteredChoiceModel<A, C>(original, filter), UtilityBasedChoiceModel<A, C> {
    override val name: String = original.name
    context(_: C, random: Random)
    fun select(): A {
        return original.select(filter.filter(choices))
    }
    context(_: C) fun probabilities() = original.probabilities(filter.filter(choices))
    context(_: C) fun utilities() = original.utilities(filter.filter(choices))

    override fun addFilter(filter: ChoiceFilter<A, C>): FilteredFixedChoiceModel<A, C> {
        return FilteredFixedChoiceModel(filter = this.filter.combine(filter), choices = choices, original = original)
    }


}