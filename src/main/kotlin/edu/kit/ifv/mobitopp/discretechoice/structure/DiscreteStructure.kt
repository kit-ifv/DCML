package edu.kit.ifv.mobitopp.discretechoice.structure

import edu.kit.ifv.mobitopp.discretechoice.UtilityAssignment
import edu.kit.ifv.mobitopp.discretechoice.UtilityFunction
import edu.kit.ifv.mobitopp.discretechoice.utility.MapBasedUtilityEnumeration
import edu.kit.ifv.mobitopp.discretechoice.utility.Rule
import edu.kit.ifv.mobitopp.discretechoice.utility.RuleBasedUtilityAssignment
import edu.kit.ifv.mobitopp.discretechoice.utility.UtilityEnumeration


class DiscreteStructure<R, A, P>(
    lambda: DiscreteStructure<R, A, P>.() -> Unit,
) : EnumeratedStructureBuilder<R, A, P>, UtilityEnumerationBuilder<R, A, P> {

    private val options = mutableMapOf<R, UtilityFunction<R, A, P>>()

    init {
        this.lambda()
    }

    override fun build(): UtilityEnumeration<R, A, P> = MapBasedUtilityEnumeration(map = options)

    override fun addUtilityFunctionByIdentifier(option: R, utilityFunction: UtilityFunction<R, A, P>) {
        require(!options.containsKey(option)) {
            "Duplicate: A utility function for $option has already been defined in this structure. " +
                    "Current elements ${options.keys} already have a utility function associated. "
        }
        options[option] = utilityFunction
    }
}

class RuleBasedStructure<A, G, P>(
    lambda: RuleBasedStructure<A, G, P>.() -> Unit,
) : RuleBasedStructureBuilder<A, G, P>, UtilityAssignmentBuilder<A, G, P> {

    val rules = mutableListOf<Rule<A, G, P>>()

    init {
        this.lambda()
    }

    override fun addUtilityFunctionByRule(
        rule: (A) -> Boolean,
        utilityFunction: UtilityFunction<A, G, P>,
    ) {
        rules.add(Rule(rule, utilityFunction))
    }

    override fun build(): UtilityAssignment<A, G, P> = RuleBasedUtilityAssignment(rules)
}
