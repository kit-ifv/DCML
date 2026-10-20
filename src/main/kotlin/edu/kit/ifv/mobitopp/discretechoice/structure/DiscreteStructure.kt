package edu.kit.ifv.mobitopp.discretechoice.structure

import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.UtilityAssignment
import edu.kit.ifv.mobitopp.discretechoice.models.UtilityFunction
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.MapBasedUtilityEnumeration
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.Rule
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.RuleBasedUtilityAssignment
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.UtilityEnumeration

/**
 * A discrete utility structure defined by an explicit map from options to utility functions.
 *
 * @param A the type representing an option/alternative.
 * @param C the type of context specific parameters available to utility functions.
 * @param P the type of parameters used by utility functions.
 * @param lambda initialization block executed on the new DiscreteStructure instance;
 *        use it to call builder methods such as [addUtilityFunctionByIdentifier] or [remove].
 */
class DiscreteStructure<A, C, P>(
    lambda: DiscreteStructure<A, C, P>.() -> Unit,
) : EnumeratedStructureBuilder<A, C, P>, UtilityEnumerationBuilder<A, C, P> {

    private val options = mutableMapOf<A, UtilityFunction<A, C, P>>()

    init {
        this.lambda()
    }

    /**
     * Build a [UtilityEnumeration] backed by the current map of options.
     *
     * @return a [UtilityEnumeration] implementation representing the current discrete mapping.
     */
    override fun build(): UtilityEnumeration<A, C, P> = MapBasedUtilityEnumeration(map = options)

    /**
     * Register a utility function for a specific option.
     *
     * @param option the option/identifier to associate with the utility function.
     * @param utilityFunction the utility function to associate with [option].
     * @throws IllegalArgumentException if a function for the same [option] has already been defined.
     */
    override fun addUtilityFunctionByIdentifier(option: A, utilityFunction: UtilityFunction<A, C, P>) {
        require(!options.containsKey(option)) {
            "Duplicate: A utilityassignment function for $option has already been defined in this structure. " +
                    "Current elements ${options.keys} already have a utilityassignment function associated. "
        }
        options[option] = utilityFunction
    }

    /**
     * Remove the utility function associated with the given option.
     *
     * @param option the option whose association should be removed.
     * @return the removed [UtilityFunction], or `null` if the option was not present.
     */
    fun remove(option: A) = options.remove(option)
}

/**
 * A builder for utility assignments defined by rules/predicates over options.
 *
 * @param A the type representing an option/alternative.
 * @param C the type of context available to utility functions.
 * @param P the type of any additional parameters used by utility functions.
 * @param lambda initialization block executed on the new RuleBasedStructure instance;
 *        use it to add rules via [addUtilityFunctionByRule].
 */
class RuleBasedStructure<A, C, P>(
    lambda: RuleBasedStructure<A, C, P>.() -> Unit,
) : RuleBasedStructureBuilder<A, C, P>, UtilityAssignmentBuilder<A, C, P> {

    /**
     * Mutable list of accumulated rules, thet were added with the [lambda] at initialization.
     */
    val rules = mutableListOf<Rule<A, C, P>>()

    init {
        this.lambda()
    }

    /**
     * Add a rule consisting of a predicate over options and its corresponding utility function.
     *
     * @param rule predicate that returns true when the provided [utilityFunction] should apply.
     * @param utilityFunction the utility function to apply when [rule] matches.
     */
    override fun addUtilityFunctionByRule(
        rule: (A) -> Boolean,
        utilityFunction: UtilityFunction<A, C, P>,
    ) {
        rules.add(Rule(rule, utilityFunction))
    }

    /**
    * Build a UtilityAssignment from the accumulated rules.
    */
    override fun build(): UtilityAssignment<A, C, P> = RuleBasedUtilityAssignment(rules)
}
