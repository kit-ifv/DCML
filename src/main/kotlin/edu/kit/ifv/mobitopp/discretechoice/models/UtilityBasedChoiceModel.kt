package edu.kit.ifv.mobitopp.discretechoice.models

/**
 * When the choice model is based on utilities, we can expect that the calculation of utilities as well as
 * probabilities is inherently available.
 */
interface UtilityBasedChoiceModel<A, C> : ChoiceModel<A, C> {

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

}