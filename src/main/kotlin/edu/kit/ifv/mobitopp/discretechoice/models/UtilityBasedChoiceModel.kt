package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random

/**
 * When the choice model is based on utilities, we can expect that the calculation of utilities as well as
 * probabilities is inherently available.
 */
interface UtilityBasedChoiceModel<A, in C> : BasicChoiceModel<A, C> {

    context(_: C)
    fun utilities(alternatives: Collection<A>): Map<A, Double> {
        return alternatives.associateWith { utility(it) }
    }

    context(_: C)
    fun utilities(vararg alternative: A) = utilities(alternative.toSet())
    context(_: C)
    fun utility(alternative: A): Double

    context(_: C)
    fun probabilities(alternatives: Collection<A>): Map<A, Double> {
        return probabilities(utilities(alternatives))
    }
    context(_: C)
    fun probabilities(vararg alternatives: A): Map<A, Double> = probabilities(alternatives.toSet())

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
    fun probabilities(utilities: Map<A, Double>): Map<A, Double>


    fun fixed(choices: Set<A>): FixedChoiceModel<A, C> {
        return FixedChoiceModelImpl(this, choices)
    }

    /**
     * @param injections a map, which maps the generated utilities for each alternative to some other utility.
     */
    context(_: C, random: Random)
    fun selectInjected(choices: Set<A>, injections: Map<A, (Double) -> Double>): A
}

fun <A, C> UtilityBasedChoiceModel<A, C>.select(filter: ChoiceFilter<A, C>): FilteredChoiceModel<A, C> {
    return FilteredChoiceModel(this, filter)
}