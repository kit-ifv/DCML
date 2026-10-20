package edu.kit.ifv.mobitopp.discretechoice.utilityassignment

import edu.kit.ifv.mobitopp.discretechoice.models.UtilityFunction

/**
 * A rule pairing a condition predicate with a utility function.
 *
 * @param A the type representing an option/alternative.
 * @param C the type of context specific parameters available to utility functions.
 * @param P the type of any additional parameters used by utility functions.
 * @param condition predicate that determines when [utilityFunction] applies.
 * @param utilityFunction the utility function applied when [condition] is satisfied.
 */
data class Rule<A, C, P>(
    private val condition: (A) -> Boolean,
    val utilityFunction: UtilityFunction<A, C, P>,
) {
    /**
     * Evaluate the rule's condition for the given alternative.
     *
     * @param alternative the option to test.
     * @return true if this rule's condition matches [alternative].
     */
    fun check(alternative: A) = condition(alternative)
}

/**
 * A utility assignment implementation that resolves the first matching rule.
 *
 * Rules are evaluated in the order they appear in [rules]; the first rule whose
 * condition returns true provides the resulting [UtilityFunction].
 *
 * @param A the type representing an option/alternative.
 * @param C the type of context specific parameters available to utility functions.
 * @param P the type of any additional parameters used by utility functions.
 * @param rules the ordered list of rules used for resolution.
 */
data class RuleBasedUtilityAssignment<A, C, P>(
    private val rules: List<Rule<A, C, P>>,

    ) : UtilityAssignment<A, C, P> {

    /**
     * Return the first utility function whose rule matches the given alternative.
     *
     * @param alternative the option to resolve.
     * @return the matching [UtilityFunction], or `null` if no rule matches.
     */
    override fun getUtilityFunctionFor(alternative: A): UtilityFunction<A, C, P>? =
        rules.firstOrNull { it.check(alternative) }?.utilityFunction
}
