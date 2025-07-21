package edu.kit.ifv.mobitopp.discretechoice.models

import edu.kit.ifv.mobitopp.discretechoice.distribution.DistributionFunction
import edu.kit.ifv.mobitopp.discretechoice.selection.SelectionFunction
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.UtilityAssignment
import kotlin.random.Random

/**
 * Basic structure of a `ChoiceModel` for a discrete situation.
 * @property A *some* choosable object.
 * @property C the characteristics for the choice situation.
 * @property P type of the parameters, which the selection is based on.
 * @param parameters the concrete values for the selection. (E.g. for a person that could be age, position, social
 * status... whatever, but this is, what the `utilityAssignment` bases its concrete utilityassignment values of each alternative
 * on.)
 */
data class DiscreteChoiceModel<A, C, P>(
    override val name: String,
    val utilityAssignment: UtilityAssignment<A, C, P>,
    val distributionFunction: DistributionFunction<A, P>,
    val selectionFunction: SelectionFunction<A>,
    val parameters: P,
) : UtilityBasedChoiceModel<A, C> {

    /**
     * @return whatever the `selectionFunction` returns when given the probabilities of all choices and the random
     * generator.
     */
    context(_: C, random: Random)
    override fun select(choices: Set<A>): A =
        selectionFunction.calculateSelection(probabilities(choices), random)

    /**
     * Selects an alternative by injecting custom modifications into the utilityassignment calculation
     * for each choice prior to selection.
     *
     * This method allows external control over the utilityassignment values by providing a map of
     * utilityassignment-modifying functions (i.e., injections) per choice. Each function transforms
     * the original utilityassignment score calculated for its corresponding choice.
     *
     * @param choices A map from each available alternative to a function that modifies
     * its original utilityassignment value. If a choice is not included in the map, the identity
     * function is used by default (i.e., no modification).
     * @param random The source of randomness used in the selection process.
     * @return The selected alternative after applying the modified utilities.
     */
    context(_: C, random: Random)
    override fun selectInjected(choices: Set<A>, injections: Map<A, (Double) -> Double>): A {
        val modifiedUtilities = utilities(choices).mapValues { (alternative, utility) ->
            val injection = injections[alternative] ?: { it }
            injection(utility)
        }
        return selectionFunction.calculateSelection(probabilities(modifiedUtilities), random)

    }

    override fun probabilities(utilities: Map<A, Double>) =
        distributionFunction.calculateProbabilities(utilities, parameters)


    /**
     * Assigns the given alternative a concrete utilityassignment value given the `parameters`.
     * @return the double utilityassignment value.
     */
    context(characteristics: C)
    override fun utility(alternative: A): Double {
        return requireNotNull(

            utilityAssignment[alternative]?.calculateUtility(alternative, characteristics, parameters)


        ) {
            "Error in model $name: \n" +
                    "No utilityassignment function was designed for $alternative"
        }
    }

    /**
     * @param choices the alternatives the new ChoiceModel works on.
     * @return an EnumeratedDiscreteChoiceModel, which behaves like this model, but works on the given `choices`.
     */
    fun with(choices: Set<A>) =
        FixedChoiceModel(
            this,
            choices
        )
}
