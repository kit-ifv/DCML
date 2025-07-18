package edu.kit.ifv.mobitopp.discretechoice.utilityassignment

import edu.kit.ifv.mobitopp.discretechoice.models.UtilityAssignment
import edu.kit.ifv.mobitopp.discretechoice.models.UtilityFunction


data class Rule<A, C, P>(
    private val condition: (A) -> Boolean,
    val utilityFunction: UtilityFunction<A, C, P>,
) {
    fun check(alternative: A) = condition(alternative)
}

data class RuleBasedUtilityAssignment<A, C, P>(
    private val rules: List<Rule<A, C, P>>,

    ) : UtilityAssignment<A, C, P> {

    override fun getUtilityFunctionFor(alternative: A): UtilityFunction<A, C, P>? =
        rules.firstOrNull { it.check(alternative) }?.utilityFunction
}
