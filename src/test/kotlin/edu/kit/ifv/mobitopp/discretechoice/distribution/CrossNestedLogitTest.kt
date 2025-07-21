package edu.kit.ifv.mobitopp.discretechoice.distribution

import edu.kit.ifv.mobitopp.discretechoice.models.DiscreteChoiceModel
import edu.kit.ifv.mobitopp.discretechoice.models.UtilityBasedChoiceModel
import edu.kit.ifv.mobitopp.discretechoice.structure.CrossNestedStructure
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.crossNestedLogit

import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals



private data class Parameters(val base: Double = 1.0)

class CrossNestedLogitTest {

    private fun UtilityBasedChoiceModel<Int, Unit>.castProbabilities(
        vararg targets: Int
    ): Map<Int, Double> {

        return context(Unit) {

            probabilities(targets.toSet())
        }
    }

    @Test
    fun simpleCrossNested() {
        val crossNestedModel = CrossNestedStructure<Int, Unit, Parameters> {
            option(1)
            option(2)
        }.withUtility {
            option(1) {
                0.0
            }
            option(2) {
                0.0
            }
        }.crossNestedLogit("SimpleCrossNestedModelForTesting").build(Parameters())

        crossNestedModel.castProbabilities(1).let {
            assertEquals(it.values.sum(), 1.0)
            assertEquals(it[1], 1.0)
        }

        crossNestedModel.castProbabilities(1, 2).let {
            assertEquals(it.values.sum(), 1.0)
            assertEquals(it[1], 0.5)
            assertEquals(it[2], 0.5)
        }

        assertThrows<IllegalArgumentException> {
            crossNestedModel.castProbabilities(3)
        }
    }

    @Test
    fun simpleCrossNestedWithAlpha() {
        var alpha1 = 0.5
        var alpha2 = 0.5
        var u1 = 0.0
        var u2 = 0.0
        var u3 = 0.0
        var lambda1 = 1.0
        var lambda2 = 1.0

        val crossNestedModel = CrossNestedStructure<Int, Unit, Parameters> {
            nest("A", lambda = lambda1) {
                option(1, alpha = alpha1)
                option(2)
            }
            nest("B", lambda = lambda2) {
                option(1, alpha = alpha2)
                option(3)
            }
        }.withUtility {
            option(1) {
                u1
            }
            option(2) {
                u2
            }
            option(3) {
                u3
            }
        }.crossNestedLogit("CrossNestedModelForTesting").build(Parameters())

        crossNestedModel.castProbabilities(1, 2, 3).let {
            assertEquals(it.values.sum(), 1.0)
            assertEquals(it[1]!!, 0.333333333, 0.0001)
            assertEquals(it[2]!!, 0.333333333, 0.0001)
            assertEquals(it[3]!!, 0.333333333, 0.0001)
        }

        alpha1 = 0.2
        alpha2 = 0.8
        crossNestedModel.castProbabilities(1, 2, 3).let {
            assertEquals(it.values.sum(), 1.0)
            assertEquals(it[1]!!, 0.333333333, 0.0001)
            assertEquals(it[2]!!, 0.333333333, 0.0001)
            assertEquals(it[3]!!, 0.333333333, 0.0001)
        }
    }

    @Test
    fun crossnestedWithLambdaIsValid() {
        var lambda1 = 1.0
        val crossNestedModel = CrossNestedStructure<Int, Unit, Parameters> {
            nest(name = "Nest 1", lambda = lambda1) {
                option(1, alpha = { 0.8 })
                option(2)
            }

            nest(name = "Nest 2", lambda = 0.000001) {
                option(1, alpha = { 0.2 })
                option(3)
            }
        }.withUtility {
            option(1) {
                0.0
            }
            option(2) {
                0.0
            }
        }.crossNestedLogit("CrossNestedModelForTestingLambda").build(Parameters())

        crossNestedModel.castProbabilities(1, 2).let {
            assertEquals(it.values.sum(), 1.0)
        }
    }
}
