package discreteChoice.models

import kotlin.random.Random

/**
 * Container object interface defining a random seed value and a `with` function. The `with` function, wraps
 * choosable objects of any type into ChoiceAlternatives objects.
 */
interface ChoiceSituation<A, X : Any> where A : ChoiceAlternative<X> {
    /**
     * Wrapping function. Wraps a choosable object `choice` into `ChoiceAlternative<X>`.
     */
    fun with(choice: X): A

    val random: Random
}

/**
 * A choice situation may contain additional appended information but is basically only a wrapper for [R].
 * Thus, the equals and hash implementation of that type can be used for mapping to a certain utility function.
 * @property choice the wrapped object.
 */
abstract class ChoiceAlternative<R : Any> {
    abstract val choice: R

    override fun equals(other: Any?): Boolean {
        if (other !is ChoiceAlternative<*>) return false
        return choice == other.choice
    }

    override fun hashCode(): Int {
        return choice.hashCode()
    }
}

/**
 * Functional interface, that contains a function `filter`, which in some way manipulates a set of choices `Set<R>`.
 */
fun interface ChoiceFilter<R> {
    fun filter(choices: Set<R>): Set<R>
}

/**
 * Choice-filter, that is the identity function.
 */
fun <R> noFilter() = ChoiceFilter<R> { choices -> choices }

/**
 * Most basic structure of a Model selecting something.
 * @property A type of the choosable objects. `A` because of "Alternative".
 * @property R the return type. Whatever this Model selects, R is the type you get. This can be a single `A` of the
 * choices, but it doesn't have to. It can be a List<`A`>, Map, Set... whatever you wish your ChoiceModel to return.
 */
interface ChoiceModel<A, R> {
    val name: String
    val choiceFilter: ChoiceFilter<A>

    /**
     * @param random some random generator.
     * @param choices the set of alternatives one is chosen from.
     * @return one, or multiple `A`. Could be something else, since `R` is generic, but the idea is that this function
     * does what you expect it to do. Return a selection of the choices.
     */
    fun select(choices: Set<A>, random: Random): R

    fun select(choices: Set<R>, random: Random, converter: (R) -> A): R = select(choices.map { converter(it) }.toSet(), random)
}

/**
 * Extension function of a ChoiceModel. Applies the `choiceFilter` of the ChoiceModel on the given `alternatives`.
 * Side effect free.
 * @param alternatives the set of objects to choose from. These are filtered, with the filter of the ChoiceModel, and
 * then, out of the leftover alternatives, chosen from.
 * @param random A function providing Random. Passed to the select function.
 * @return `R`... whatever is selected after filtering the alternatives.
 */
fun <A, R : Any> ChoiceModel<A, R>.filterAndSelect(
    alternatives: Set<A>,
    random: Random
): R {
    val filtered = choiceFilter.filter(alternatives).toSet()

    require(filtered.isNotEmpty()) {
        "Choice model $name cannot evaluate du to empty filtered choice set!\n" +
            "  - choice set before filter: $alternatives"
    }

    return select(filtered, random)
}

/**
 * Convenience function, which constructs `FixedChoicesModel` out of any `ChoiceModel`.
 * @param choices the choices the returned FixedChoicesModel has.
 * @return a `FixedChoicesModel` with the given `choices` as its choices.
 */
fun <A, R : Any> ChoiceModel<A, R>.fixed(choices: Set<R>): FixedChoicesModel<A, R> where A : ChoiceAlternative<R> =
    EnumeratedChoiceModel<R, A>(this, choices)

/**
 * @return another ChoiceModel, based on this one, but with `newFilter` piped before all existing filters.
 */
fun <A, R : Any> ChoiceModel<A, R>.addFilter(
    newFilter: ChoiceFilter<A>
): ChoiceModel<A, R> = ChoiceModelDecorator<A, R>(
    delegate = this,
    newFilter = ChoiceFilter { choices -> this.choiceFilter.filter(newFilter.filter(choices)) }
)

/**
 * Interface of a ChoiceModel which has a non-changeable set of choices. Additionally, has `filterAndSelect` function,
 * applying the choiceFilter to the alternatives before selecting from them.
 *
 */
interface FixedChoicesModel<A, R : Any> : ChoiceModel<A, R> where A : ChoiceAlternative<R> {
    val choices: Set<R>

    /**
     * Applies the choice filter onto the choices before selecting from these filtered choices.
     * @param situation Object holding random-seed and wrapping function for options of type A.
     */
    fun filterAndSelect(situation: ChoiceSituation<A, R>): R {
        val alternatives = choices.map { situation.with(it) }.toSet()
        return filterAndSelect(alternatives, situation.random)
    }
}

/**
 * @return another FixedChoiceModel, based on this one, but with `newFilter` piped before all existing filters.
 */
fun <A, R : Any> FixedChoicesModel<A, R>.addFilter(
    newFilter: ChoiceFilter<A>
): FixedChoicesModel<A, R> where A : ChoiceAlternative<R> = FixedChoicesModelDecorator<A, R>(
    delegate = this,
    newFilter = ChoiceFilter { choices -> this.choiceFilter.filter(newFilter.filter(choices)) }
)

/**
 * Decorator class providing the option to overwrite the getter for name and choiceFilter of a ChoiceModel.
 * The selection of choices is done by the `delegate`.
 */
open class ChoiceModelDecorator<A, R : Any>(
    protected open val delegate: ChoiceModel<A, R>,
    private val newName: String? = null,
    private val newFilter: ChoiceFilter<A>? = null,
    private val combineFilters: Boolean = false
) : ChoiceModel<A, R> {

    private val combinedFilter: ChoiceFilter<A>? by lazy {
        takeIf { combineFilters }?.let {
            newFilter?.let {
                ChoiceFilter { choices ->
                    delegate.choiceFilter.filter(
                        newFilter.filter(choices)
                    )
                }
            }
        }
    }

    override val name: String
        get() = newName ?: delegate.name

    override val choiceFilter: ChoiceFilter<A>
        get() = combinedFilter ?: newFilter ?: delegate.choiceFilter

    override fun select(choices: Set<A>, random: Random): R = delegate.select(choices, random)
}

/**
 * Decorator for a FixedChoicesModel (`delegate`). Provides the option to define a new set of choices (and name and
 * filter).
 */
class FixedChoicesModelDecorator<A, R : Any>(
    protected override val delegate: FixedChoicesModel<A, R>,
    newName: String? = null,
    newFilter: ChoiceFilter<A>? = null,
    combineFilters: Boolean = false,
    private val newChoices: Set<R>? = null,
) : ChoiceModelDecorator<A, R>(
    delegate, newName, newFilter, combineFilters
), FixedChoicesModel<A, R> where A : ChoiceAlternative<R> {

    override val choices: Set<R>
        get() = newChoices ?: delegate.choices
}

/**
 * A FixedChoicesModel based on another `ChoiceModel`.
 */
data class EnumeratedChoiceModel<R : Any, A : ChoiceAlternative<R>>(
    private val choiceModel: ChoiceModel<A, R>,
    override val choices: Set<R>,
) : ChoiceModel<A, R> by choiceModel, FixedChoicesModel<A, R>


/**
 * ChoiceModel which selects one of the `choices` with equal probability.
 */
class RandomChoiceModel<A, R : Any>(
    override val name: String,
    override val choices: Set<R>,
    override val choiceFilter: ChoiceFilter<A> = noFilter(),
) : FixedChoicesModel<A, R> where A : ChoiceAlternative<R> {

    override fun select(choices: Set<A>, random: Random): R {
        val index = random.nextInt(choices.size)
        return choices.toList()[index].choice
    }
}

/**
 * A FixedChoicesModel which sequentially traverses it's set `choices` (the one set on creation). The first option,
 * that is also present in the set of choices provided on the `select` call, gets selected.
 */
class FixedOrderChoiceModel<A, R : Any>( // TODO what is this used for?
    override val name: String,
    choices: Set<R>,
    override val choiceFilter: ChoiceFilter<A>
) : FixedChoicesModel<A, R> where A : ChoiceAlternative<R> {

    private val secretChoices = choices
    override val choices: Set<R>
        get() = secretChoices

    override fun select(choices: Set<A>, random: Random): R {
        val index = choices.map {it.choice}
        return secretChoices.first { it in index }
    }
}
