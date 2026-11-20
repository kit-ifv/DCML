package edu.kit.ifv.mobitopp.discretechoice.models.gumbel

import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.SingularUtilityFunction
import kotlin.random.Random
import kotlin.test.Test

class GumbelChoiceModelTest {

    @Test
    fun properSelection() {
        val gumbel = GumbelChoiceModel<Int, Unit, Unit>(
            epsilonSampler = IndependentGumbel(),
            parameters = Unit,
            utilityAssignment = SingularUtilityFunction {a, c, p ->
                .0
            },
            name = "Friend",
        )
        val b: Random = Random(42)
        val texts = (0 until 100000).map {
            it
        }.toSet()

        val selections =  IntArray(texts.size) {
            0
        }
        repeat(100000) {
            val selection = context(Unit, b) {
                gumbel.select(texts)
            }
            selections[selection] += 1

        }
        println(selections.toList())


    }
}