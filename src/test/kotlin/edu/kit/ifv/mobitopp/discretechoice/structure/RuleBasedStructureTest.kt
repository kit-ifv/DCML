package edu.kit.ifv.mobitopp.discretechoice.structure

import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.openMultinomialLogit
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class RuleBasedStructureTest {
    @Test
    fun correctConstruction() {
        val structure = RuleBasedStructure<Int, Unit, Unit> {
            rule({it % 2 == 0}) {
                -it.toDouble()
            }
            rule({it % 2 == 1}) {
                it.toDouble()
            }
        }

        val dcm = structure.openMultinomialLogit("Test").build(Unit)
        context(Unit) {
            assertEquals(1.0, dcm.utility(1))
            assertEquals(-2.0, dcm.utility(2))
        }
    }
}