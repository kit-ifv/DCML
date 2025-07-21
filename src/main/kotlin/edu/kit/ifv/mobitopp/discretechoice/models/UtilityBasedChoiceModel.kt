package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random

/**
 * When the choice model is based on utilities, we can expect that the calculation of utilities as well as
 * probabilities is inherently available.
 */
interface UtilityBasedChoiceModel<A, C> : BasicChoiceModel<A, C> {

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
    fun probabilities(utilities: Map<A, Double>): Map<A, Double>


    override fun addFilter(filter: ChoiceFilter<A, C>): FilteredChoiceModel<A, C> {
        return FilteredChoiceModel(this, filter)
    }

    fun fixed(choices: Set<A>): FixedChoiceModel<A, C> {
        return FixedChoiceModel(this, choices)
    }

    context(_: C, random: Random)
    fun selectInjected(choices: Set<A>, injections: Map<A, (Double) -> Double>): A
}