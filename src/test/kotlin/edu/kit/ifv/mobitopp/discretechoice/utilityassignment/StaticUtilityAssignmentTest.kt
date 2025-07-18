package edu.kit.ifv.mobitopp.discretechoice.utilityassignment

import edu.kit.ifv.mobitopp.discretechoice.models.UtilityFunction
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class StaticUtilityAssignmentTest {
    @Test

    fun staticChoiceModelInvocation() {
        val dcm = UtilityFunction<Int, Unit, String> {a, c, s ->
            a.toDouble() * s.length

        }.openMultinomialLogit("Example choice model").build("Wololo")

        context(Unit) {
            assertEquals(6.0, dcm.utility(1))
            assertEquals(12.0, dcm.utility(2))
        }
    }

}