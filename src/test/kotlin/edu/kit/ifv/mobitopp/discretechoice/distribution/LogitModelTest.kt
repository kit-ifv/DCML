package discreteChoice.discreteChoice.discreteChoice.edu.kit.ifv.mobitopp.discretechoice.distribution

import edu.kit.ifv.mobitopp.discretechoice.DiscreteChoiceModel
import edu.kit.ifv.mobitopp.discretechoice.structure.DiscreteStructure
import edu.kit.ifv.mobitopp.discretechoice.utility.multinomialLogit
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

private class ControlledRandom(seed: Int): Random() {
    private val random = Random(seed)
    /**
     * Gets the next random [bitCount] number of bits.
     *
     * Generates an `Int` whose lower [bitCount] bits are filled with random values and the remaining upper bits are zero.
     *
     * @param bitCount number of bits to generate, must be in range 0..32, otherwise the behavior is unspecified.
     *
     * @sample samples.random.Randoms.nextBits
     */
    override fun nextBits(bitCount: Int): Int {
        return random.nextBits(bitCount)
    }
    var randomNumber: Double? = null
        get() {
            return field.also { field = null }
        }
    override fun nextDouble(): Double {
        val draw = randomNumber
        return draw ?: random.nextDouble()
    }

}
class LogitModelTest {
    private val discreteChoiceModel: DiscreteChoiceModel<Int, Unit, Unit> = DiscreteStructure<Int, Unit, Unit> {
                option(1) {

                    0.0
                }
                option(2) {
                    1.0
                }
                option(3) {
                    0.0
                }

            }.multinomialLogit("Test model").build(Unit).model

    private val selection = setOf(1, 2, 3)
    private val random =
        _root_ide_package_.discreteChoice.discreteChoice.discreteChoice.edu.kit.ifv.mobitopp.discretechoice.distribution.ControlledRandom(
            1
        )
    @Test
    fun utilityManipulation()  {
        val injections: Map<Int, (Double) -> Double> = mapOf(2 to { 0.0 })


        assertEquals(2, select(0.3335, injections))
        assertEquals(1, select(0.3332, injections))
    }

    private class Params(
        val d: Double = 1.0
    )
    @Test
    fun globalObjectPassing()  {
        val choiceModel = DiscreteStructure<Int, Pair<Int, Double>, Params> {
            option(1) { alternative, characteristics ->

                characteristics.second + d
            }
            option(2) { option, global ->
                option.toDouble() * global.first + d

            }

        }.multinomialLogit("Test model").build(Params())

        val output = context(1 to .05) {
            choiceModel.model.probabilities(setOf(1, 2))
        }
        println(output)
    }
    private fun select(randomValue: Double? = null, injection: Map<Int, (Double) -> Double>): Int {
        random.randomNumber = randomValue
        return context(Unit) {discreteChoiceModel.selectInjected(selection, injection, random)}
    }
}