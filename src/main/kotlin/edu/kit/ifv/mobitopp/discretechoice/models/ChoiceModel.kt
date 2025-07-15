package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random




/**
 * Interface of a ChoiceModel which has a non-changeable set of choices. Additionally, has `filterAndSelect` function,
 * applying the choiceFilter to the alternatives before selecting from them.
 *
 */
interface FixedChoicesModel<A, G> : ChoiceModel<A, G> {
    val choices: Set<A>

//    /**
//     * Applies the choice filter onto the choices before selecting from these filtered choices.
//     * @param situation Object holding random-seed and wrapping function for options of type A.
//     */
//    fun filterAndSelect(situation: ChoiceSituation<A, R>): A {
//        val alternatives = choices.map { situation.with(it) }.toSet()
//        return filterAndSelect(alternatives, situation.random)
//    }
}

///**
// * Decorator class providing the option to overwrite the getter for name and choiceFilter of a ChoiceModel.
// * The selection of choices is done by the `delegate`.
// */
//open class ChoiceModelDecorator<A, R : Any>(
//    protected open val delegate: ChoiceModel<A, R>,
//    private val newName: String? = null,
//    private val newFilter: ChoiceFilter<A>? = null,
//    private val combineFilters: Boolean = false
//) : ChoiceModel<A, R> {
//
//    private val combinedFilter: ChoiceFilter<A>? by lazy {
//        takeIf { combineFilters }?.let {
//            newFilter?.let {
//                ChoiceFilter { choices ->
//                    delegate.choiceFilter.filter(
//                        newFilter.filter(choices)
//                    )
//                }
//            }
//        }
//    }
//
//    override val name: String
//        get() = newName ?: delegate.name
//
//    override val choiceFilter: ChoiceFilter<A>
//        get() = combinedFilter ?: newFilter ?: delegate.choiceFilter
//
//    override fun select(choices: Set<A>, random: Random): R = delegate.select(choices, random)
//}

///**
// * Decorator for a FixedChoicesModel (`delegate`). Provides the option to define a new set of choices (and name and
// * filter).
// */
//class FixedChoicesModelDecorator<A, R : Any>(
//    protected override val delegate: FixedChoicesModel<A, R>,
//    newName: String? = null,
//    newFilter: ChoiceFilter<A>? = null,
//    combineFilters: Boolean = false,
//    private val newChoices: Set<R>? = null,
//) : ChoiceModelDecorator<A, R>(
//    delegate, newName, newFilter, combineFilters
//), FixedChoicesModel<A, R> where A : ChoiceAlternative<R> {
//
//    override val choices: Set<R>
//        get() = newChoices ?: delegate.choices
//}

/**
 * A FixedChoicesModel based on another `ChoiceModel`.
 */
data class EnumeratedChoiceModel<R, A>(
    private val choiceModel: ChoiceModel<R, A>,
    override val choices: Set<R>,
) : ChoiceModel<R, A> by choiceModel, FixedChoicesModel<R, A>


/**
 * ChoiceModel which selects one of the `choices` with equal probability.
 */
class RandomChoiceModel<A, G>(
    override val name: String,
    override val choices: Set<A>,
) : FixedChoicesModel<A, G> {
    context(global: G, random: Random)
    override fun select(choices: Set<A>): A {
        val index = random.nextInt(choices.size)
        return choices.toList()[index]
    }
}

