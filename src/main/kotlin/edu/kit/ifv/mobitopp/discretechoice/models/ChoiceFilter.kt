package edu.kit.ifv.mobitopp.discretechoice.models


/**
 * Functional interface, that contains a function `filter`, which in some way manipulates a set of choices `Set<R>`.
 */
fun interface ChoiceFilter<A, C> {
    context(_: C)
    fun filter(choices: Set<A>): Set<A>
    companion object {
        fun <A, C> noFilter(): ChoiceFilter<A, C> {
            return ChoiceFilter {it}
        }
    }

    fun combine(other: ChoiceFilter<A, C>): ChoiceFilter<A, C> {
        return ChoiceFilter {
            this.filter(it).intersect(other.filter(it))
        }
    }
}
