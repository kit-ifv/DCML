package edu.kit.ifv.mobitopp.discretechoice.models


/**
 * If a choice model has a static choice set and a filter.
 */
interface FilterableFixedChoiceModel<A, C> : FixedChoiceModel<A, C>, FilterableChoiceModel<A, C>

fun <A, C> FilterableChoiceModel<A, C>.fixed(choices: Set<A>): FilterableFixedChoiceModel<A, C> {
    return FilterableFixedChoiceWrapper(filter = this.filter, choices = choices, choiceModel = this)
}

fun <A, C> FixedChoiceModel<A, C>.addFilter(filter: ChoiceFilter<A>): FilterableFixedChoiceModel<A, C> {
    return FilterableFixedChoiceWrapper(filter = filter, choices = this.choices, choiceModel = this)
}

data class FilterableFixedChoiceWrapper<A, C>(
    override val filter: ChoiceFilter<A>,
    override val choices: Set<A>,
    val choiceModel: UtilityBasedChoiceModel<A, C>

): FilterableFixedChoiceModel<A, C>, UtilityBasedChoiceModel<A, C> by choiceModel