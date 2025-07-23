package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random

/**
 * In case that we actually have a utility based choice model, we can provide the extra functions like utility and
 * probability.
 */
open class FilteredChoiceModel<A, C>(
    override val original: UtilityBasedChoiceModel<A, C>,
    filter: ChoiceFilter<A, C>,
) : BasicFilteredChoiceModel<A, C>(original, filter, original.name), UtilityBasedChoiceModel<A, C> by original {

    override val name = original.name

    context(_: C, _: Random)
    override fun select(choices: Set<A>): A {
        val filteredChoices = choices.filter { filter.filter(it) }.toSet()
        return original.select(filteredChoices)
    }

    context(_: C)
    override fun utilities(alternatives: Collection<A>): Map<A, Double> {
        return original.utilities(alternatives)
    }
    override fun addFilter(filter: ChoiceFilter<A, C>): FilteredChoiceModel<A, C> {
        return FilteredChoiceModel(original, this.filter + filter)
    }

}