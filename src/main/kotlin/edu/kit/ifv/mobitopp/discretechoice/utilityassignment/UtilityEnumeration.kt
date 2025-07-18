package edu.kit.ifv.mobitopp.discretechoice.utilityassignment

import edu.kit.ifv.mobitopp.discretechoice.models.UtilityAssignment
import edu.kit.ifv.mobitopp.discretechoice.models.UtilityFunction


interface UtilityEnumeration<A, C, P> : UtilityAssignment<A, C, P> {
    val options: Set<A>
}

data class MapBasedUtilityEnumeration<A, C, P>(
    private val map: Map<A, UtilityFunction<A, C, P>>,
) : UtilityEnumeration<A, C, P> {
    override fun getUtilityFunctionFor(alternative: A): UtilityFunction<A, C, P>? = map[alternative]
    override val options: Set<A> get() = map.keys
}
