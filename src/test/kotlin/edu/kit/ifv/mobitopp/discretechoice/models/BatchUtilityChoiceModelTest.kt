package edu.kit.ifv.mobitopp.discretechoice.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
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

    @Test
    fun testSubsetProbabilityEqual() {
        val subset = IntArray(10) { it }
        val subsetSize = subset.size
        val floatProbs = context(null) {
            floatCM.probabilities(subset.toSet())
        }
        assertEquals(subsetSize, floatProbs.size)
        assertAlmostEqual(1.0, floatProbs.values.sum())
        floatProbs.forEach { (_, probability) ->
            assertAlmostEqual(probability, floatProbs[0]!!)
        }

        val doubleProbs = context(null) {
            doubleCM.probabilities(subset.toSet())
        }
        assertEquals(subsetSize, doubleProbs.size)
        assertAlmostEqual(1.0, doubleProbs.values.sum())
        doubleProbs.forEach { (_, probability) ->
            assertAlmostEqual(probability, doubleProbs[0]!!)
        }
    }

    @Test
    fun outOfBoundsAlternatives() {
        val subset = IntArray(10) { it } + 100 // 100 is not a valid alternative with the cm models
        context(null) {
            assertThrows<IllegalStateException> {
                floatCM.probabilities(subset.toSet())
            }
        }

        context(null) {
            assertThrows<IllegalStateException> {
                doubleCM.probabilities(subset.toSet())
            }
        }
    }

    @Test
    fun modifyAlternatives() {
        val newAlternativesSubset = (IntArray(10) { it }).toSet()
        val alternativesSize = newAlternativesSubset.size
        val newFloatModel = floatCM.fixed(newAlternativesSubset)
        val newDoubleModel = doubleCM.fixed(newAlternativesSubset)

        val floatProbs = context(null) {
            newFloatModel.probabilities(newAlternativesSubset)
        }
        assertEquals(alternativesSize, floatProbs.size)
        assertAlmostEqual(1.0, floatProbs.values.sum())
        floatProbs.forEach { (_, probability) ->
            assertAlmostEqual(probability, floatProbs[0]!!)
        }

        val doubleProbs = context(null) {
            newDoubleModel.probabilities(newAlternativesSubset.toSet())
        }
        assertEquals(alternativesSize, doubleProbs.size)
        assertAlmostEqual(1.0, doubleProbs.values.sum())
        doubleProbs.forEach { (_, probability) ->
            assertAlmostEqual(probability, doubleProbs[0]!!)
        }

        context(null) {
            assertThrows<IllegalStateException> {
                newFloatModel.probabilities(IntArray(20){ it }.toSet())
            }
        }

        context(null) {
            assertThrows<IllegalStateException> {
                newDoubleModel.probabilities(IntArray(20){ it }.toSet())
            }
        }
    }

    fun assertAlmostEqual(a: Double, b: Double) {
        assert((a - b).absoluteValue < 0.000001)
    }
}