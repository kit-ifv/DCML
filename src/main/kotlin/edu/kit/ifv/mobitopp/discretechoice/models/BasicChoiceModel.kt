package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random

/**
 * Most basic structure of a Model selecting something.
 * @property A type of the choosable objects. `A` because of "Alternative".
 * @property C the characteristics influencing the decision.
 */
interface BasicChoiceModel<A, C> {
    val name: String

    /**
     * @param random some random generator.
     * @param choices the set of alternatives one is chosen from.
     * @return one chosen alternative
     */
    context(_: C, _: Random)
    fun select(choices: Set<A>): A

    /**
     * This function allows a vararg call, instead of having to wrap to a set manually.
     * Future implementations could avoid the creation of a set to speed up the selection process.
     */
    context(_: C, _: Random)
    fun select(choices: Array<out A>): A {
        return select(choices.toSet())
    }
    fun addFilter(filter: ChoiceFilter<A, C>): BasicFilteredChoiceModel<A, C> {
        return BasicFilteredChoiceModel(this, filter, name)
    }
}


