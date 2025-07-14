package discreteChoice.utility

import discreteChoice.UtilityAssignment
import discreteChoice.UtilityFunction


data class Rule<A, P>(
    private val condition: (A) -> Boolean,
    val utilityFunction: UtilityFunction<A, P>,
) {
    fun check(alternative: A) = condition(alternative)
}

data class RuleBasedUtilityAssignment<R, A, P>(
    private val rules: List<Rule<R, P>>,

    ) : UtilityAssignment<R, A, P> {

    override fun getUtilityFunctionFor(alternative: R): UtilityFunction<R, P>? =
        rules.firstOrNull { it.check(alternative) }?.utilityFunction
}
