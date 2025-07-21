package edu.kit.ifv.mobitopp.discretechoice.models

import edu.kit.ifv.mobitopp.discretechoice.structure.DiscreteStructure
import edu.kit.ifv.mobitopp.discretechoice.structure.forOptions
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.multinomialLogit
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.openMultinomialLogit
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class FilterChoiceModelTest {

    val discreteStructure = DiscreteStructure<Int, Unit, Unit> {
        forOptions(1..5) { _, _ ->
            0.0
        }
    }
    val simpleChoiceModel = discreteStructure.multinomialLogit("Test model").build(Unit)
    val openChoiceModel = discreteStructure.openMultinomialLogit("Different model").build(Unit)
    val firstFilter = ChoiceFilter<Int, Unit> {
        it - 1
    }
    val secondFilter = ChoiceFilter<Int, Unit> {
        it - 3
    }

    @Test
    fun filterChoiceModelTest() {
        val filterModel = simpleChoiceModel.addFilter(firstFilter)
        val secondFilterModel = filterModel.addFilter(secondFilter)
        context(Unit, Random(1)) {

            val comparison =  (0..100).map { simpleChoiceModel.select() }.toSet()
            val first =(0..100).map { filterModel.select() }.toSet()
            val second =(0..100).map { secondFilterModel.select() }.toSet()

            filterModel.probabilities()
            assertEquals(comparison.size, 5)
            assertEquals(first.size, 4)
            assertEquals(second.size, 3)


        }


    }
    @Test
    fun openModelTest() {
        val filterModel = openChoiceModel.addFilter(firstFilter)
        val secondFilterModel = filterModel.addFilter(secondFilter)
        val options = setOf(1, 2, 3)
        context(Unit, Random(1)) {
            val first =  (0..100).map { filterModel.select(options) }.toSet()
            val second =  (0..100).map { secondFilterModel.select(options) }.toSet()
            assertEquals(first.size, 2)
            assertEquals(second.size, 1)
        }
    }
}