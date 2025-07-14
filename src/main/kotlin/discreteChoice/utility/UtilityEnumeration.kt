package discreteChoice.utility

import discreteChoice.UtilityAssignment
import discreteChoice.UtilityFunction
import discreteChoice.incubator.GFUtilityFunction
import discreteChoice.incubator.GUtilityAssignment


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

interface UtilityEnumeration<R, A, P> : UtilityAssignment<R, A, P> {
    val options: Set<R>
}

data class MapBasedUtilityEnumeration<R, A, P>(
    private val map: Map<R, UtilityFunction<R, P>>,
) : UtilityEnumeration<R, A, P> {
    override fun getUtilityFunctionFor(alternative: R): UtilityFunction<R, P>? = map[alternative]
    override val options: Set<R> get() = map.keys
}

data class GMapBasedUtilityEnumeration<A, G, P>(private val map: Map<A, GFUtilityFunction<A, G, P>>) :
    GUtilityAssignment<A, G, P> {
    override fun getUtilityFunctionFor(alternative: A): GFUtilityFunction<A, G, P> {
        return map.getValue(alternative)
    }
}