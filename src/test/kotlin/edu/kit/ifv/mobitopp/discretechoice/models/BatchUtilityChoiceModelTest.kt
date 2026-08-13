package edu.kit.ifv.mobitopp.discretechoice.models

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.math.absoluteValue
import kotlin.test.Test

class TestDoubleBatchCM(
    originalChoices: Set<Int>,
    override val name: String = "Test-Double",
) : BatchUtilityChoiceModel<Unit?, Unit?, Int>(
    choices = originalChoices,
    parameters = null
) {
    val size = choices.size

    val equalUtil = DoubleArray(size) { 5.0 }

    context(characteristic: Unit?)
    override fun Unit?.generateUtilitiesArray(): DoubleArray = equalUtil
}

class TestFloatBatchCM(
    originalChoices: Set<Int>,
    override val name: String = "Test-Float"
) : FloatBatchUtilityChoiceModel<Unit?, Unit?, Int>(
    choices = originalChoices,
    parameters = null
) {
    val size = choices.size

    val equalUtil = FloatArray(size) { 5f }

    context(characteristic: Unit?)
    override fun Unit?.generateUtilitiesArray(): FloatArray = equalUtil
}

class BatchUtilityChoiceModelTest {
    val doubleCM = TestDoubleBatchCM(
        IntArray(20) { it }.toSet()
    )
    val floatCM = TestFloatBatchCM(
        IntArray(20) { it }.toSet()
    )

    @Test
    fun testProbabilityEqual() {

        val floatProbs = context(null) {
            floatCM.probabilities()
        }
        assertEquals(20, floatProbs.size)
        assertAlmostEqual(1.0, floatProbs.values.sum())
        floatProbs.forEach { (_, probability) ->
            assertAlmostEqual(probability, floatProbs[0]!!)
        }

        val doubleProbs = context(null) {
            doubleCM.probabilities()
        }
        assertEquals(20, doubleProbs.size)
        assertAlmostEqual(1.0, doubleProbs.values.sum())
        doubleProbs.forEach { (_, probability) ->
            assertAlmostEqual(probability, doubleProbs[0]!!)
        }
    }

    fun assertAlmostEqual(a: Double, b: Double) {
        assert((a - b).absoluteValue < 0.000001)
    }
}