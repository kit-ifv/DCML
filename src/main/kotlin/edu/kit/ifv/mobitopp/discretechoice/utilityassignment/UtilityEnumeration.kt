package edu.kit.ifv.mobitopp.discretechoice.utilityassignment

import edu.kit.ifv.mobitopp.discretechoice.models.ArrayBackedFixedChoiceModel
import edu.kit.ifv.mobitopp.discretechoice.models.UtilityFunction


interface UtilityEnumeration<A, in C, in P> : UtilityAssignment<A, C, P> {
    val options: Set<A>
}

data class MapBasedUtilityEnumeration<A, C, P>(
    private val map: Map<A, UtilityFunction<A, C, P>>,
) : UtilityEnumeration<A, C, P> {
    override fun getUtilityFunctionFor(alternative: A): UtilityFunction<A, C, P>? = map[alternative]
    override val options: Set<A> get() = map.keys
}

data class SingularUtilityFunction<A, in C, in P>(
    private val utilityFunction: UtilityFunction<A, C, P>,
): UtilityAssignment<A, C, P> {
    override fun getUtilityFunctionFor(alternative: A): UtilityFunction<A, C, P> {
        return utilityFunction
    }
}

fun <A, C, P> UtilityAssignment<A, C, P>.toArrayChoiceModel(fixedOptions: Set<A>, parameter: P, name: String): ArrayBackedFixedChoiceModel<A, C, P> {
    return ArrayBackedFixedChoiceModel(
        utilityAssignment = this,
        choices = fixedOptions,
        parameters = parameter,
        name = name,
    )
}