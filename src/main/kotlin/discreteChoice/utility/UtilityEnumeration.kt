package discreteChoice.utility

import discreteChoice.UtilityAssignment
import discreteChoice.UtilityFunction
import discreteChoice.incubator.GFUtilityFunction
import discreteChoice.incubator.GUtilityAssignment
import discreteChoice.models.ChoiceAlternative


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

interface UtilityEnumeration<R : Any, A, P> : UtilityAssignment<R, A, P> where A : ChoiceAlternative<R> {
    val options: Set<R>
}

data class MapBasedUtilityEnumeration<R : Any, A, P>(
    private val map: Map<R, UtilityFunction<A, P>>,
) : UtilityEnumeration<R, A, P> where A : ChoiceAlternative<R> {
    override fun getUtilityFunctionFor(alternative: A): UtilityFunction<A, P>? = map[alternative.choice]
    override val options: Set<R> get() = map.keys
}

data class GMapBasedUtilityEnumeration<A, G, P>(private val map: Map<A, GFUtilityFunction<A, G, P>>) :
    GUtilityAssignment<A, G, P> {
    override fun getUtilityFunctionFor(alternative: A): GFUtilityFunction<A, G, P> {
        return map.getValue(alternative)
    }
}