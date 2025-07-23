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
        return original.select(filteredSet())
    }
    context(_: C)
    private fun filteredSet() = choices.filter { filter.filter(it) }.toSet()
    context(_: C) override fun probabilities() = original.probabilities(filteredSet())
    context(_: C) override fun utilities() = original.utilities(filteredSet())


    context(_: C) override fun utilities(vararg alternative: A): Map<A, Double> {
        return original.utilities(alternative.filter { filter.filter(it) }.toSet())
    }
    override fun addFilter(filter: ChoiceFilter<A, C>): FilteredFixedChoiceModel<A, C> {
        return FilteredFixedChoiceModel(filter = this.filter + filter, choices = choices, original = original)
    }


}