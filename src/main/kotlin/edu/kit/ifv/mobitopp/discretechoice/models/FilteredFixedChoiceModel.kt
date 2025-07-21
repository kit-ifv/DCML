package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random




class FilteredFixedChoiceModel<A, C>(
    original: UtilityBasedChoiceModel<A, C>,
    override val choices: Set<A>,
    filter: ChoiceFilter<A, C>,



    ): FilteredChoiceModel<A, C>(original, filter), UtilityBasedChoiceModel<A, C>, FixedChoiceModel<A, C> {
    override val name: String = original.name
    context(_: C, random: Random)
    override fun select(): A {
        return original.select(filter.filter(choices))
    }
    context(_: C) override fun probabilities() = original.probabilities(filter.filter(choices))
    context(_: C) override fun utilities() = original.utilities(filter.filter(choices))


    context(_: C) override fun utilities(vararg alternative: A): Map<A, Double> {
        return original.utilities(filter.filter(alternative.toSet()))
    }
    override fun addFilter(filter: ChoiceFilter<A, C>): FilteredFixedChoiceModel<A, C> {
        return FilteredFixedChoiceModel(filter = this.filter.combine(filter), choices = choices, original = original)
    }


}