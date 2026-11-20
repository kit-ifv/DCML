package edu.kit.ifv.mobitopp.discretechoice.models.gumbel

import edu.kit.ifv.mobitopp.discretechoice.models.FixedChoiceModel
import kotlin.random.Random

class FixedGumbelChoiceModel<A, C>(
    override val choices: Set<A>,
): FixedChoiceModel<A, C> {


    context(_: C, random: Random)
    override fun select(): A {
        TODO("Not yet implemented")
    }

    override val name: String
        get() = TODO("Not yet implemented")

    /**
     * @param random some random generator.
     * @param choices the set of alternatives one is chosen from.
     * @return one chosen alternative
     */
    context(_: C, _: Random)
    override fun select(choices: List<A>): A {
        TODO("Not yet implemented")
    }

    context(_: C)
    override fun utility(alternative: A): Double {
        TODO("Not yet implemented")
    }

    /**
     * Converts a map of utility values into a corresponding map of selection
     * probabilities.
     *
     * @param utilities A mapping from each alternative to its computed utility
     *                  value. Utilities can be any real number. The relative
     *                  differences drive the probability outcomes. (Depending on the implementation of this function,
     *                  utilities have to be in a certain range. It is recommended to document this behavior
     *                  separately).
     * @return A mapping from each alternative to its selection probability.
     *         All probabilities sum to 1.0 across all entries.
     *
     * @throws IllegalArgumentException if [utilities] is empty.
     */
    override fun probabilities(utilities: Map<A, Double>): Map<A, Double> {
        TODO("Not yet implemented")
    }

    /**
     * @param injections a map, which maps the generated utilities for each alternative to some other utility.
     */
    context(_: C, random: Random)
    override fun selectInjected(
        choices: List<A>,
        injections: Map<A, (Double) -> Double>,
    ): A {
        TODO("Not yet implemented")
    }
}