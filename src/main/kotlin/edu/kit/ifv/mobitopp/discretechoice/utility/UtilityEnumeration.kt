package edu.kit.ifv.mobitopp.discretechoice.utility

import edu.kit.ifv.mobitopp.discretechoice.UtilityAssignment
import edu.kit.ifv.mobitopp.discretechoice.UtilityFunction


// fun <R : Any, A, P> enumeratedDiscreteChoiceModel(
//    name: String,
//    choiceFilter: ChoiceFilter<A>,
//    utilityAssignment: UtilityEnumeration<R, A, P>,
//    distributionFunction: DistributionFunction<A, P>,
//    selectionFunction: SelectionFunction<A>,
//    parameters: P,
// ): DiscreteChoiceModel<R, A, P> where A : ChoiceAlternative<R> = DiscreteChoiceModel<R, A, P>(
//    name,
//    utilityAssignment.options,
//    choiceFilter,
//    utilityAssignment,
//    distributionFunction,
//    selectionFunction,
//    parameters
// )

interface UtilityEnumeration<A, G, P> : UtilityAssignment<A, G, P> {
    val options: Set<A>
}

data class MapBasedUtilityEnumeration<A, G, P>(
    private val map: Map<A, UtilityFunction<A, G, P>>,
) : UtilityEnumeration<A, G, P> {
    override fun getUtilityFunctionFor(alternative: A): UtilityFunction<A, G, P>? = map[alternative]
    override val options: Set<A> get() = map.keys
}
