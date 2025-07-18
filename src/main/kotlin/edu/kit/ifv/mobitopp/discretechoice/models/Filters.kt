package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random


/**
 * Functional interface, that contains a function `filter`, which in some way manipulates a set of choices `Set<R>`.
 */
fun interface ChoiceFilter<A> {
    fun filter(choices: Set<A>): Set<A>
}
