package discreteChoice.utility

import discreteChoice.UtilityAssignment
import discreteChoice.UtilityFunction
import discreteChoice.models.ChoiceAlternative


data class Rule<A, P>(
    private val condition: (A) -> Boolean,
    val utilityFunction: UtilityFunction<A, P>
) {
    fun check(alternative: A) = condition(alternative)
}

data class RuleBasedUtilityAssignment<R : Any, A, P>(
    private val rules: List<Rule<A, P>>

) : UtilityAssignment<R, A, P> where A : ChoiceAlternative<R> {

    override fun getUtilityFunctionFor(alternative: A): UtilityFunction<A, P>? =
        rules.firstOrNull { it.check(alternative) }?.utilityFunction
}
