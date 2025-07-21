package edu.kit.ifv.mobitopp.discretechoice.structure

import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.UtilityAssignment
import edu.kit.ifv.mobitopp.discretechoice.models.UtilityFunction
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.MapBasedUtilityEnumeration
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.Rule
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.RuleBasedUtilityAssignment
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.UtilityEnumeration


class DiscreteStructure<A, C, P>(
    lambda: DiscreteStructure<A, C, P>.() -> Unit,
) : EnumeratedStructureBuilder<A, C, P>, UtilityEnumerationBuilder<A, C, P> {

    private val options = mutableMapOf<A, UtilityFunction<A, C, P>>()

    init {
        this.lambda()
    }

    override fun build(): UtilityEnumeration<A, C, P> = MapBasedUtilityEnumeration(map = options)

    override fun addUtilityFunctionByIdentifier(option: A, utilityFunction: UtilityFunction<A, C, P>) {
        require(!options.containsKey(option)) {
            "Duplicate: A utilityassignment function for $option has already been defined in this structure. " +
                    "Current elements ${options.keys} already have a utilityassignment function associated. "
        }
        options[option] = utilityFunction
    }
}

class RuleBasedStructure<A, C, P>(
    lambda: RuleBasedStructure<A, C, P>.() -> Unit,
) : RuleBasedStructureBuilder<A, C, P>, UtilityAssignmentBuilder<A, C, P> {

    val rules = mutableListOf<Rule<A, C, P>>()

    init {
        this.lambda()
    }

    override fun addUtilityFunctionByRule(
        rule: (A) -> Boolean,
        utilityFunction: UtilityFunction<A, C, P>,
    ) {
        rules.add(Rule(rule, utilityFunction))
    }

    override fun build(): UtilityAssignment<A, C, P> = RuleBasedUtilityAssignment(rules)
}
