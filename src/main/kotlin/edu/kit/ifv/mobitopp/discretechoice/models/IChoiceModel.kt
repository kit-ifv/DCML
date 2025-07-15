package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random

/**
 * Most basic structure of a Model selecting something.
 * @property A type of the choosable objects. `A` because of "Alternative".
 * @property C the characteristics influencing the decision.
 */
interface ChoiceModel<A, C> {
    val name: String

    /**
     * @param random some random generator.
     * @param choices the set of alternatives one is chosen from.
     * @return one chosen alternative
     */
    context(characteristics: C, random: Random)
    fun select(choices: Set<A>): A
}