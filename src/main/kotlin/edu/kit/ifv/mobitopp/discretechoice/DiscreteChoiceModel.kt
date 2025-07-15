package edu.kit.ifv.mobitopp.discretechoice

import edu.kit.ifv.mobitopp.discretechoice.models.ChoiceModel
import edu.kit.ifv.mobitopp.discretechoice.models.FixedChoicesModel
import edu.kit.ifv.mobitopp.discretechoice.utility.associateWithNotNull
import kotlin.random.Random

/**
 * A utility function takes in an alternative and a parameter object and returns the utility of said alternative.
 */
fun interface UtilityFunction<A, G, P> {
    fun calculateUtility(alternative: A, global: G, parameterObject: P): Double
}

/**
 * Functional interface defining the higher order function (function returning a function :]) `getUtilityFunctionFor`,
 * which returns a `UtilityFunction` for the given `alternative`. Independent of other possibly existing alternatives.
 * (Also defines a getter function, which does the same as `getUtilityFunctionFor(alternative)`.)
 * @property P the type of the parameters. The utility always gets assigned based on concrete instance values (age=25,
 * position=Karlsruhe... whatever). `P` is the type which contains these concrete values. It's the type the returned
 * Utility function expects.
 */
fun interface UtilityAssignment<A, G, P> {
    fun getUtilityFunctionFor(alternative: A): UtilityFunction<A, G, P>?
    operator fun get(alternative: A) = getUtilityFunctionFor(alternative)
}

/**
 * A distribution function takes in a map, which contains each choosable object(alternative) with their associated
 * utility functions already calculated. Also takes a parameter object and returns a map of calculated probabilities
 * from the given alternatives.
 */
fun interface DistributionFunction<A, P> {
    fun calculateProbabilities(utilities: Map<A, Double>, parameters: P): Map<A, Double>
}

/**
 * A functional interface defining a structure for going from probabilities to a concrete selected element.
 * @property X type of the objects to be selected.
 * @param calculateSelection the provided function, which executes the selection on some `options`.
 * @return one of the objects of type `X` from the `options`.
 */
fun interface SelectionFunction<X> {
    /**
     * @param options a map, mapping each possible object to its probability. (All probabilities have to sum up to 1?)
     * @return one of the `X` objects present in `options`.
     */
    fun calculateSelection(options: Map<X, Double>, random: Random): X
}

/**
 * Basic structure of a `ChoiceModel` for a discrete situation.
 * @property R *some* choosable object.
 * @property A a `ChoiceAlternative<R>` type.
 * @property P type of the parameters, which the selection is based on.
 * @param parameters the concrete values for the selection. (E.g. for a person that could be age, position, social
 * status... whatever, but this is, what the `utilityAssignment` bases its concrete utility values of each alternative
 * on.)
 */
data class DiscreteChoiceModel<A, G, P>(
    override val name: String,
    val utilityAssignment: UtilityAssignment<A, G, P>,
    val distributionFunction: DistributionFunction<A, P>,
    val selectionFunction: SelectionFunction<A>,
    val parameters: P,
) : ChoiceModel<A, G> {

    /**
     * @return whatever the `selectionFunction` returns when given the probabilities of all choices and the random
     * generator.
     */
    context(global: G, random: Random)
    override fun select(choices: Set<A>): A =
        selectionFunction.calculateSelection(probabilities(choices), random)

    /**
     * Selects an alternative by injecting custom modifications into the utility calculation
     * for each choice prior to selection.
     *
     * This method allows external control over the utility values by providing a map of
     * utility-modifying functions (i.e., injections) per choice. Each function transforms
     * the original utility score calculated for its corresponding choice.
     *
     * @param choices A map from each available alternative to a function that modifies
     * its original utility value. If a choice is not included in the map, the identity
     * function is used by default (i.e., no modification).
     * @param random The source of randomness used in the selection process.
     * @return The selected alternative after applying the modified utilities.
     */
    context(global: G)
    fun selectInjected(choices: Set<A>, injections: Map<A, (Double) -> Double>, random: Random): A {
        val modifiedUtilities = utilities(choices).mapValues { (alternative, utility) ->
            val injection = injections[alternative] ?: { it }
            injection(utility)
        }
        return selectionFunction.calculateSelection(probabilities(modifiedUtilities), random)

    }

    /**
     * @return a map with each alternative mapped to its probability.
     */
    context(global: G)
    fun probabilities(alternatives: Set<A>) = probabilities(utilities(alternatives))


    fun probabilities(utilities: Map<A, Double>) =
        distributionFunction.calculateProbabilities(utilities, parameters)

    /**
     * @return a map with each alternative mapped to its utility value.
     */
    context(global: G)
    fun utilities(alternatives: Set<A>): Map<A, Double> =
        alternatives.associateWithNotNull { utility(it) }

    /**
     * Assigns the given alternative a concrete utility value given the `parameters`.
     * @return the double utility value.
     */
    context(global: G)
    fun utility(alternative: A): Double {
        return requireNotNull(

            utilityAssignment[alternative]?.calculateUtility(alternative, global, parameters)


        ) {
            "Error in model $name: \n" +
                    "No utility function was designed for ${alternative}"
        }
    }

    /**
     * @param choices the alternatives the new ChoiceModel works on.
     * @return an EnumeratedDiscreteChoiceModel, which behaves like this model, but works on the given `choices`.
     */
    fun with(choices: Set<A>) = EnumeratedDiscreteChoiceModel<A, G, P>(this, choices)
}

/**
 * A ChoiceModel based on another DiscreteChoiceModel (`model`). Behaves like the `model` if the `model` had the
 * `choices` as its alternatives.
 */
data class EnumeratedDiscreteChoiceModel<R, A, P>(
    val model: DiscreteChoiceModel<R, A, P>,
    override val choices: Set<R>,
) : ChoiceModel<R, A> by model, FixedChoicesModel<R, A> {
    context(global: A, random: Random)
    override fun select(choices: Set<R>): R = model.select(choices)
}
