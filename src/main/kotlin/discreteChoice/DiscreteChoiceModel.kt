package discreteChoice

import discreteChoice.models.ChoiceAlternative
import discreteChoice.models.ChoiceFilter
import discreteChoice.models.ChoiceModel
import discreteChoice.models.FixedChoicesModel
import discreteChoice.utility.associateWithNotNull
import kotlin.random.Random

/**
 * A utility function takes in an alternative and a parameter object and returns the utility of said alternative.
 */
fun interface UtilityFunction<A, P> {
    fun calculateUtility(alternative: A, parameterObject: P): Double
}

/**
 * Functional interface defining the higher order function (function returning a function :]) `getUtilityFunctionFor`,
 * which returns a `UtilityFunction` for the given `alternative`. Independent of other possibly existing alternatives.
 * (Also defines a getter function, which does the same as `getUtilityFunctionFor(alternative)`.)
 * @property P the type of the parameters. The utility always gets assigned based on concrete instance values (age=25,
 * position=Karlsruhe... whatever). `P` is the type which contains these concrete values. It's the type the returned
 * Utility function expects.
 */
fun interface UtilityAssignment<R : Any, A, P> where A : ChoiceAlternative<R> {
    fun getUtilityFunctionFor(alternative: A): UtilityFunction<A, P>?
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
data class DiscreteChoiceModel<R : Any, A, P>(
    override val name: String,
    override val choiceFilter: ChoiceFilter<A>,
    val utilityAssignment: UtilityAssignment<R, A, P>,
    val distributionFunction: DistributionFunction<A, P>,
    val selectionFunction: SelectionFunction<A>,
    val parameters: P,
) : ChoiceModel<A, R> where A : ChoiceAlternative<R> {

    override fun select(choices: Set<A>, random: Random): R =
        selectionFunction.calculateSelection(probabilities(choices), random).choice

    fun probabilities(alternatives: Set<A>) =
        distributionFunction.calculateProbabilities(utilities(alternatives), parameters)

    fun utilities(alternatives: Set<A>): Map<A, Double> =
        alternatives.associateWithNotNull { utility(it) }

    /**
     * Assigns the given alternative a concrete utility value given the `parameters`
     */
    fun utility(alternative: A): Double = requireNotNull(
        utilityAssignment[alternative]?.calculateUtility(alternative, parameters)
    ) {
        "Error in model $name: \n" +
            "No utility function was designed for ${alternative.choice}"
    }

    fun with(choices: Set<R>) = EnumeratedDiscreteChoiceModel<R, A, P>(this, choices)
}

data class EnumeratedDiscreteChoiceModel<R : Any, A, P>(
    val model: DiscreteChoiceModel<R, A, P>,
    override val choices: Set<R>,
) : ChoiceModel<A, R> by model, FixedChoicesModel<A, R> where A : ChoiceAlternative<R> {

    override fun select(choices: Set<A>, random: Random): R = model.select(choices, random)
}
