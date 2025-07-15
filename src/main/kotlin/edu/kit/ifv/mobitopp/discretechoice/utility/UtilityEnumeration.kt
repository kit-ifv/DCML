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

interface UtilityEnumeration<A, C, P> : UtilityAssignment<A, C, P> {
    val options: Set<A>
}

data class MapBasedUtilityEnumeration<A, C, P>(
    private val map: Map<A, UtilityFunction<A, C, P>>,
) : UtilityEnumeration<A, C, P> {
    override fun getUtilityFunctionFor(alternative: A): UtilityFunction<A, C, P>? = map[alternative]
    override val options: Set<A> get() = map.keys
}
