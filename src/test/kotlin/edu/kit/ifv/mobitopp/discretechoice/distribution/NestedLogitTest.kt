package edu.kit.ifv.mobitopp.discretechoice.distribution

import edu.kit.ifv.mobitopp.discretechoice.structure.NestedStructure
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.nestedLogit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class NestedLogitTest {

    private val choiceModel = NestedStructure<Options, Unit, RedbusParameters> {
        option(Options.CAR) {
            0.0
        }
        nest("Bus", { lambdaBus }) {
            option(Options.RED_BUS) {
                0.0
            }
            option(Options.BLUE_BUS) {
                0.0
            }
        }
    }.nestedLogit("Red bus Blue bus choice model")

    @Test
    fun redBusBlueBus() {
        val model = choiceModel.build(identical).model
        val result = context(Unit) { model.probabilities(Options.ALL_VALID) }
        assertEquals(result[Options.RED_BUS], 0.25)
        assertEquals(result[Options.BLUE_BUS], 0.25)
        assertEquals(result[Options.CAR], 0.5)
    }

    @Test
    fun invariantRedBus() {
        val model = choiceModel.build(different).model
        val result = context(Unit) { model.probabilities(Options.ALL_VALID) }
        assertEquals(result[Options.RED_BUS], 1.0 / 3)
        assertEquals(result[Options.BLUE_BUS], 1.0 / 3)
        assertEquals(result[Options.CAR], 1.0 / 3)
    }

    @Test
    fun duplicateOptionsAreCaught() {
        assertThrows<IllegalArgumentException> {
            NestedStructure<Options, Unit, RedbusParameters> {
                option(Options.CAR) {
                    1.0
                }
                option(Options.CAR) {
                    1.0
                }
            }
        }
    }

    @Test
    fun emptyStructuresAreCaught() {
        assertThrows<IllegalArgumentException> {
            NestedStructure<Options, Unit, RedbusParameters> { }
        }
    }

    @Test
    fun emptyNestsAreCaught() {
        assertThrows<IllegalArgumentException> {
            NestedStructure<Options, Unit, RedbusParameters> {
                nest("A") {
                    nest("B") {
                        nest("C") {
                            nest("D") {

                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun unassociatedElementsGiveMessage() {
        assertThrows<IllegalArgumentException> {
            val dcm = choiceModel.build(
                RedbusParameters(
                    1.0
                )
            ).model
            context(Unit) {
                dcm.probabilities(Options.ALL_VALID + Options.OTHER_ILLEGAL_OPTION)
            }

        }
    }


    private class RedbusParameters(val lambdaBus: Double) {
        val pedestrian = DifferentParameters.fromRedbusParameters(this)
    }

    private class DifferentParameters(val ped: Double) {
        companion object {

            fun fromRedbusParameters(redbusParameters: RedbusParameters): DifferentParameters {
                return redbusParameters.internalConverter()
            }

            private fun RedbusParameters.internalConverter(): DifferentParameters {
                return DifferentParameters(
                    ped = lambdaBus
                )
            }
        }
    }

    private val identical =
        RedbusParameters(
            Double.MIN_VALUE
        )
    private val different =
        RedbusParameters(
            1.0
        )

    private enum class Options {
        RED_BUS, BLUE_BUS, CAR, OTHER_ILLEGAL_OPTION;

        companion object {
            val ALL_VALID = setOf(RED_BUS, BLUE_BUS, CAR)
        }
    }


}