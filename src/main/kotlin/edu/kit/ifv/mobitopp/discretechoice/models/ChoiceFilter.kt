package edu.kit.ifv.mobitopp.discretechoice.models


/**
 * Functional interface, that contains a function `filter`, which tests whether an alternative A is considered valid for the se of options.
 */
fun interface ChoiceFilter<in A, in C> {
    context(characteristics: C)
    fun filter(alternative: A): Boolean
    companion object {
        val noFilter = ChoiceFilter<Any, Any> { true }
    }

}
operator fun <A, C> ChoiceFilter<A, C>.plus(other: ChoiceFilter<A, C>): ChoiceFilter<A, C> {
    return ChoiceFilter {this.filter(it) && (other.filter(it))}
}
