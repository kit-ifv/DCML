package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random

/**
 * Interface of a ChoiceModel which has a non-changeable set of choices. Since the choices are known beforehand, the
 * simple select function can be provided
 *
 */
interface FixedChoiceModel<A, C> : UtilityBasedChoiceModel<A, C> {
    val choices: Set<A>

    context(_:C, _: Random)
    fun select() = select(choices)
}

fun <A, C> UtilityBasedChoiceModel<A, C>.fixed(choices: Set<A>): FixedChoiceModel<A, C> {
    return FixedChoiceWrapper(this, choices)
}

data class FixedChoiceWrapper<A, C>(
    val original: UtilityBasedChoiceModel<A, C>,
    override val choices: Set<A>,
) : FixedChoiceModel<A, C>, UtilityBasedChoiceModel<A, C> by original