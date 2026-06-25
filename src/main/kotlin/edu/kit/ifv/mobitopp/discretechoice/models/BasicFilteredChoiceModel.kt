package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random

/**
 * The standard wrapper class for a choice model to apply a filter before selecting.
 * Must be invariant because of the choice filter.
 */
open class BasicFilteredChoiceModel<A, in C>(
    open val original: BasicChoiceModel<A, C>,
    val filter: ChoiceFilter<A, C>,
    override val name: String
): BasicChoiceModel<A, C> {


    context(_: C, _: Random)
    override fun select(choices: Set<A>): A {
        val filteredChoices = choices.filter { filter.filter(it) }.toSet()
        return original.select(filteredChoices)
    }

}

fun <A, C> BasicFilteredChoiceModel<A, C>.addFilter(filter: ChoiceFilter<A, C>): BasicFilteredChoiceModel<A, C> {
    return BasicFilteredChoiceModel(original, this.filter + filter, this.name)
}