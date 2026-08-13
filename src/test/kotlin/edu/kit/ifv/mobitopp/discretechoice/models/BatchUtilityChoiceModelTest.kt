package edu.kit.ifv.mobitopp.discretechoice.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import kotlin.math.absoluteValue
import kotlin.random.Random
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

    @Test
    fun utility() {
        val choices = IntArray(20) { it }
        choices.forEach { choice ->
            context(null) {
                assertEquals(5.0, floatCM.utility(choice))
                assertEquals(5.0, doubleCM.utility(choice))
            }
        }

        context(null) {
            assertThrows<IllegalStateException>{
                floatCM.utility(-1)
            }
            assertThrows<IllegalStateException>{
                doubleCM.utility(-1)
            }

            assertThrows<IllegalStateException>{
                floatCM.utility(21)
            }
            assertThrows<IllegalStateException>{
                doubleCM.utility(21)
            }
        }
    }

    @Test
    fun mapProbabilities() {
        val utilities = IntArray(20) { it } .associate {
            if(it == 0) it to 1.0
            else it to Double.NEGATIVE_INFINITY
        }

        var probabilitiesMap = floatCM.probabilities(utilities)
        assertEquals(utilities.size, probabilitiesMap.size)
        assertAlmostEqual(1.0, probabilitiesMap.values.sum())
        assertAlmostEqual(1.0, probabilitiesMap[0]!!)

        probabilitiesMap = doubleCM.probabilities(utilities)
        assertEquals(utilities.size, probabilitiesMap.size)
        assertAlmostEqual(1.0, probabilitiesMap.values.sum())
        assertAlmostEqual(1.0, probabilitiesMap[0]!!)
    }

    @Test
    fun mapProbabilitiesSubset() {
        val utilities = IntArray(10) { it  + 5} .associate {
            if(it == 5) it to 1.0
            else it to Double.NEGATIVE_INFINITY
        }

        var probabilitiesMap = floatCM.probabilities(utilities)
        assertEquals(utilities.size, probabilitiesMap.size)
        assertAlmostEqual(1.0, probabilitiesMap.values.sum())
        assertAlmostEqual(1.0, probabilitiesMap[5]!!)

        probabilitiesMap = doubleCM.probabilities(utilities)
        assertEquals(utilities.size, probabilitiesMap.size)
        assertAlmostEqual(1.0, probabilitiesMap.values.sum())
        assertAlmostEqual(1.0, probabilitiesMap[5]!!)
    }

    @Test
    fun select() {
        val choices = IntArray(20) { it }.toSet()

        var chose: List<Int> = List(1000) {
            context(null, Random(0)) {
                floatCM.select()
            }
        }
        assert(choices.containsAll(chose))

        chose = List(1000) {
            context(null, Random(0)) {
                doubleCM.select()
            }
        }
        assert(choices.containsAll(chose))
    }

    @Test
    fun selectSubset() {
        val subset = IntArray(10) { it  + 5}.toSet()

        var chose: List<Int> = List(1000) {
            context(null, Random(0)) {
                floatCM.select(subset)
            }
        }
        assert(subset.containsAll(chose))

        chose = List(1000) {
            context(null, Random(0)) {
                doubleCM.select(subset)
            }
        }
        assert(subset.containsAll(chose))
    }

    @Test
    fun selectInjectedSubset() {
        val subset = IntArray(10) { it  + 5 }.toSet()
        val utilities = IntArray(10) { it  + 5} .associate {
            if(it == 5 || it == 6) it to 1.0
            else it to Double.NEGATIVE_INFINITY
        }
        var chose: List<Int> = List(1000) {
            context(null, Random(0)) {
                floatCM.selectInjected(subset, utilities.mapValues { (elem, utility) ->
                    { _ -> utility }
                })
            }
        }
        chose.forEach {
            assert(it == 5 || it == 6)
        }

        chose = List(1000) {
            context(null, Random(0)) {
                doubleCM.selectInjected(subset, utilities.mapValues { (elem, utility) ->
                    { _ -> utility }
                })
            }
        }
        chose.forEach {
            assert(it == 5 || it == 6)
        }
    }

    @Test
    fun selectInjectedWeirdMap() {
        val subset = IntArray(10) { it  + 5 }.toSet()
        val utilities = IntArray(22) { it } .associate {
            if(it == 5 || it == 6) it to 1.0
            else it to Double.NEGATIVE_INFINITY
        }
        var chose: List<Int> = List(1000) {
            context(null, Random(0)) {
                floatCM.selectInjected(subset, utilities.mapValues { (elem, utility) ->
                    { _ -> utility }
                })
            }
        }
        chose.forEach {
            assert(it == 5 || it == 6)
        }

        chose = List(1000) {
            context(null, Random(0)) {
                doubleCM.selectInjected(subset, utilities.mapValues { (elem, utility) ->
                    { _ -> utility }
                })
            }
        }
        chose.forEach {
            assert(it == 5 || it == 6)
        }
    }

    fun assertAlmostEqual(a: Double, b: Double) {
        assert((a - b).absoluteValue < 0.000001) {"a: $a, b: $b"}
    }
}