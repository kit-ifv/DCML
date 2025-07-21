package edu.kit.ifv.mobitopp.discretechoice.utilityassignment

import edu.kit.ifv.mobitopp.discretechoice.models.UtilityFunction

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