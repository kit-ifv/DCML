package edu.kit.ifv.mobitopp.discretechoice.selection

import kotlin.random.Random

fun interface FloatSelectionFunctionArray{
    fun calculateSelection(probabilities: FloatArray, random: Random, ): Int = pick(probabilities, random.nextDouble())
    fun pick(probabilities: FloatArray, random: Double): Int
}