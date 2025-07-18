package edu.kit.ifv.mobitopp.discretechoice.structure

import edu.kit.ifv.mobitopp.discretechoice.models.UtilityFunction
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.openMultinomialLogit
import kotlin.test.Test
import kotlin.test.assertEquals

class InheritanceStructureTest {

    private open class A {
        val first: Double = 0.0
        open val second: Double = 1.0
    }
    private class B : A() {
        override val second: Double = 2.0
    }
    @Test
    fun inheritanceIsSupported() {
        val dcm = UtilityFunction<A, Unit, Unit> {a, _, _ ->
            a.first + a.second
        }.openMultinomialLogit("Test model").build(Unit)

        context(Unit) {
            val alternativeA = A()
            assertEquals(1.0, dcm.utility(alternativeA))
            val alternativeB = B()
            assertEquals(2.0, dcm.utility(alternativeB))
            val utilities = dcm.utilities(setOf(alternativeA, alternativeB))
            assertEquals(2.0, utilities[alternativeB]!!)
        }

        val enumerated = dcm.with(setOf(A()))
        context(Unit) {
            assertEquals(enumerated.probabilities().size, 1)
            enumerated
        }
    }
}