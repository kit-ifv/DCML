package edu.kit.ifv.mobitopp.discretechoice.utilityassignment

import edu.kit.ifv.mobitopp.discretechoice.models.DiscreteChoiceModel
import edu.kit.ifv.mobitopp.discretechoice.models.SelectionFunction
import edu.kit.ifv.mobitopp.discretechoice.models.UtilityAssignment
import edu.kit.ifv.mobitopp.discretechoice.models.UtilityFunction
import edu.kit.ifv.mobitopp.discretechoice.distribution.MultinomialLogit
import edu.kit.ifv.mobitopp.discretechoice.selection.RandomWeightedSelect

/**
 * This assignment strategy assigns the same utility function to all potential candidates.
 */
class StaticUtilityAssignment<A, C, P>(val utilityFunction: UtilityFunction<A, C, P>) : UtilityAssignment<A, C, P> {
    override fun getUtilityFunctionFor(alternative: A): UtilityFunction<A, C, P> = utilityFunction
}


fun <A, C, P> UtilityFunction<A, C, P>.openMultinomialLogit(name: String, selectionFunction: SelectionFunction<A> = RandomWeightedSelect()): DiscreteModelBuilder<A, C, P> {
    return DiscreteModelBuilder { parameters ->

        DiscreteChoiceModel(
            name = name,
            utilityAssignment = StaticUtilityAssignment(this),
            distributionFunction = MultinomialLogit(),
            selectionFunction = selectionFunction,
            parameters = parameters
        )
    }}