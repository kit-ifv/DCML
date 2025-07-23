package edu.kit.ifv.mobitopp.discretechoice.structure

import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.multinomialLogit
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

private enum class Option {
    FIRST, SECOND, THIRD;
}
private class Parameters(
    private val parameterSet: MutableMap<Option, Parameter> = mutableMapOf(),
): Map<Option, Parameter> by parameterSet {

    operator fun set(option: Option, parameter: Parameter) {
        parameterSet[option] = parameter
    }
}

private class Characteristics(
    val number: Int,
)

private class Parameter(
    val a: Double,
    val b: Double,
)
class EnumeratedStructureBuilderTest {
    private val structure = DiscreteStructure<Option, Characteristics, Parameters> {
        loadFromMap(Option.entries) { alt, char->
            (alt.ordinal + 1) * char.number + a + b

        }
    }

    @Test
    fun bulkEditCalculatesCorrectUtility() {
        val p = Parameters()
        p[Option.FIRST] = Parameter(10.0, 100.0)
        p[Option.SECOND] = Parameter(20.0, 200.0)
        p[Option.THIRD] = Parameter(30.0, 300.0)

        val dcm = structure.multinomialLogit("Example choice model").build(p)
        context(Characteristics(1)) {
            assertEquals(dcm.utility(Option.FIRST), 100 + 10 + 1.0)
            assertEquals(dcm.utility(Option.SECOND), 200 + 20 + 2.0)
            assertEquals(dcm.utility(Option.THIRD), 300 + 30 + 3.0)
        }

        context(Characteristics(0)) {
            assertEquals(dcm.utility(Option.FIRST), 100 + 10.0)
            assertEquals(dcm.utility(Option.SECOND), 200 + 20.0)
            assertEquals(dcm.utility(Option.THIRD), 300 + 30.0)
        }
    }
    @Test
    fun parameterObjectIsMutable() {
        val p = Parameters()
        p[Option.FIRST] = Parameter(10.0, 100.0)
        val dcm = structure.multinomialLogit("Example choice model").build(p)
        context(Characteristics(1)) {
            assertEquals(dcm.utility(Option.FIRST), 100 + 10 + 1.0)
            p[Option.FIRST] = Parameter(.0,.0)
            assertEquals(dcm.utility(Option.FIRST), 1.0)
        }


    }
    @Test
    fun missingParameter() {
        val p = Parameters()
        p[Option.FIRST] = Parameter(10.0, 100.0)
        p[Option.SECOND] = Parameter(20.0, 200.0)
        val dcm = structure.multinomialLogit("Example choice model").build(p)
        context(Characteristics(1)) {
            assertThrows<NoSuchElementException> {

                dcm.utility(Option.THIRD)
            }

        }
    }
}