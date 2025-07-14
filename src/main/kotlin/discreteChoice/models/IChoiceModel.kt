package discreteChoice.models

import kotlin.random.Random

/**
 * Most basic structure of a Model selecting something.
 * @property A type of the choosable objects. `A` because of "Alternative".
 * @property G the global object for encapsulating global influences that do not appear on the alternative.
 */
interface ChoiceModel<A, G> {
    val name: String

    /**
     * @param random some random generator.
     * @param choices the set of alternatives one is chosen from.
     * @return one chosen alternative
     */
    context(global: G, random: Random)
    fun select(choices: Set<A>): A
}