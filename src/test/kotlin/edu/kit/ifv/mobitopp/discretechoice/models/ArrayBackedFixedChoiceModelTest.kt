package edu.kit.ifv.mobitopp.discretechoice.models

import edu.kit.ifv.mobitopp.discretechoice.structure.DiscreteStructure
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.random.Random
import kotlin.test.Test

class ArrayBackedFixedChoiceModelTest {

    @Test
    fun selectDoesNotCauseIssues() {

        val structure = DiscreteStructure<Int, Unit, Unit> {
            option(1) {
                -200000.0
            }
            option(2) {
                0.0
            }
            option(3) {
                .0
            }
        }
        val choiceModel = ArrayBackedFixedChoiceModel(
            utilityAssignment = structure.build(),
            parameters = Unit,
            name = "Test build"
        )
        val selection = setOf(1)
        val rng = Random(42)
        repeat(1000) {
            val choice = context(Unit, rng) {

                choiceModel.select(selection)
            }
            assertEquals(choice, 1)
        }
        val selectionNext = setOf(1, 2)
        repeat(100) {
            val choice = context(Unit, rng) {

                choiceModel.select(selectionNext)
            }
            assertEquals(choice,2)
        }


    }

}