package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random

interface TrulyFixedChoiceModel<A, in C> {
    val choices: Set<A>

    context(characteristic: C, random: Random)
    fun select(): A
}