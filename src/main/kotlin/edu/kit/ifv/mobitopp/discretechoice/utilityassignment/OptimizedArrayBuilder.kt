package edu.kit.ifv.mobitopp.discretechoice.utilityassignment

import edu.kit.ifv.mobitopp.discretechoice.models.FixedChoiceModel
import edu.kit.ifv.mobitopp.discretechoice.structure.UtilityEnumerationBuilder

class OptimizedArrayBuilder<A, C, P>(
    private val utilityBuilder: UtilityEnumerationBuilder<A, C, P>,
    private val name: String
): EnumeratedDiscreteModelBuilder<A, C, P> {
    override fun build(parameters: P): FixedChoiceModel<A, C> {
        return utilityBuilder.optimizedMultinomialLogit(name, parameters)
    }
}