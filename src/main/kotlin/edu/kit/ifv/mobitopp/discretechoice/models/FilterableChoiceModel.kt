package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random

/**
 * Adds a filter to select valid elements for the choice model
 */
interface FilterableChoiceModel<A, C> : UtilityBasedChoiceModel<A, C> {
    val filter: ChoiceFilter<A>
    context(_: C, random: Random)
    fun filterAndSelect(alternatives: Set<A>): A {
        return select(filter.filter(alternatives))
    }
}

/**
 * Wrapper class to hold the filter, since implemenations may add additional filters, this overrides the select
 * method.
 */
data class FilterChoiceWrapper<A, C>(
    val original: UtilityBasedChoiceModel<A, C>,
    override val filter: ChoiceFilter<A>,
): FilterableChoiceModel<A, C>, UtilityBasedChoiceModel<A, C> by original {
    context(_: C, _: Random)
    override fun select(choices: Set<A>): A {
        return original.select(filter.filter(choices))
    }
}


fun <A, C> UtilityBasedChoiceModel<A, C>.addFilter(filter: ChoiceFilter<A>): FilterChoiceWrapper<A, C> {
    return FilterChoiceWrapper(this, filter)
}