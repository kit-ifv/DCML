package edu.kit.ifv.mobitopp.discretechoice.selection

import kotlin.random.Random

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