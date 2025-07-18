package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random

/**
 * A utilityassignment function takes in an alternative and a parameter object and returns the utilityassignment of said alternative.
 */
fun interface UtilityFunction<A, C, P> {
    fun calculateUtility(alternative: A, characteristics: C, parameterObject: P): Double
}

/**
 * Functional interface defining the higher order function (function returning a function :]) `getUtilityFunctionFor`,
 * which returns a `UtilityFunction` for the given `alternative`. Independent of other possibly existing alternatives.
 * (Also defines a getter function, which does the same as `getUtilityFunctionFor(alternative)`.)
 * @property P the type of the parameters. The utilityassignment always gets assigned based on concrete instance values (age=25,
 * position=Karlsruhe... whatever). `P` is the type which contains these concrete values. It's the type the returned
 * Utility function expects.
 */
fun interface UtilityAssignment<A, C, P> {
    fun getUtilityFunctionFor(alternative: A): UtilityFunction<A, C, P>?
    operator fun get(alternative: A) = getUtilityFunctionFor(alternative)
}

/**
 * A distribution function takes in a map, which contains each choosable object(alternative) with their associated
 * utilityassignment functions already calculated. Also takes a parameter object and returns a map of calculated probabilities
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
    fun selectInjected(choices: Set<A>, injections: Map<A, (Double) -> Double>): A {
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
        EnumeratedDiscreteChoiceModel(
            this,
            choices
        )
}

/**
 * A ChoiceModel based on another DiscreteChoiceModel (`model`). Behaves like the `model` if the `model` had the
 * `choices` as its alternatives.
 */
data class EnumeratedDiscreteChoiceModel<A, C, P>(
    val model: DiscreteChoiceModel<A, C, P>,
    override val choices: Set<A>,
) : UtilityBasedChoiceModel<A, C> by model, FixedChoiceModel<A, C> {
    context(_: C, _: Random)
    override fun select(choices: Set<A>): A = model.select(choices)

    context(_: C)
    fun probabilities() = probabilities(choices)

    context(_: C)
    fun utilities() = utilities(choices)

    context(_: C, random: Random)
    fun selectInjected(choices: Set<A>, injections: Map<A, (Double) -> Double>): A =
        model.selectInjected(choices, injections)

    context(_: C, random: Random)
    fun selectInjected(injections: Map<A, (Double) -> Double>): A = selectInjected(choices, injections)
}
