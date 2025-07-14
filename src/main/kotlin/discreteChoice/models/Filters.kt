package discreteChoice.models

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

///**
// * Extension function of a ChoiceModel. Applies the `choiceFilter` of the ChoiceModel on the given `alternatives`.
// * Side effect free.
// * @param alternatives the set of objects to choose from. These are filtered, with the filter of the ChoiceModel, and
// * then, out of the leftover alternatives, chosen from.
// * @param random A function providing Random. Passed to the select function.
// * @return `R`... whatever is selected after filtering the alternatives.
// */
//fun <A, R : Any> ChoiceModel<A, R>.filterAndSelect(
//    alternatives: Set<A>,
//    random: Random
//): R {
//    val filtered = choiceFilter.filter(alternatives).toSet()
//
//    require(filtered.isNotEmpty()) {
//        "Choice model $name cannot evaluate du to empty filtered choice set!\n" +
//            "  - choice set before filter: $alternatives"
//    }
//
//    return select(filtered, random)
//}

///**
// * @return another ChoiceModel, based on this one, but with `newFilter` piped before all existing filters.
// */
//fun <A, R : Any> ChoiceModel<A, R>.addFilter(
//    newFilter: ChoiceFilter<A>
//): ChoiceModel<A, R> = ChoiceModelDecorator<A, R>(
//    delegate = this,
//    newFilter = ChoiceFilter { choices -> this.choiceFilter.filter(newFilter.filter(choices)) }
//)

///**
// * @return another FixedChoiceModel, based on this one, but with `newFilter` piped before all existing filters.
// */
//fun <A, R : Any> FixedChoicesModel<A, R>.addFilter(
//    newFilter: ChoiceFilter<A>
//): FixedChoicesModel<A, R> where A : ChoiceAlternative<R> = FixedChoicesModelDecorator<A, R>(
//    delegate = this,
//    newFilter = ChoiceFilter { choices -> this.choiceFilter.filter(newFilter.filter(choices)) }
//)