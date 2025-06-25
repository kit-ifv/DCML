package discreteChoice

import discreteChoice.models.ChoiceAlternative
import discreteChoice.structure.DiscreteStructure
import discreteChoice.utility.multinomialLogit
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

private class InternalAlternative(override val choice: Int): ChoiceAlternative<Int>()
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
    private val discreteChoiceModel: DiscreteChoiceModel<Int, ChoiceAlternative<Int>, Unit> = DiscreteStructure<Int, ChoiceAlternative<Int>, Unit> {
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

    private val selection = setOf(1, 2, 3).map { InternalAlternative(it) }.toSet()
    private val random = ControlledRandom(1)
    @Test
    fun utilityManipulation()  {
        val injections: Map<Int, (Double) -> Double> = mapOf(2 to { 0.0 })


        println(discreteChoiceModel.probabilities(selection))
        assertEquals(2, select(0.3335, injections))
        assertEquals(1, select(0.3332, injections))
    }
    private fun select(randomValue: Double? = null, injection: Map<Int, (Double) -> Double>): Int {
        random.randomNumber = randomValue
        return discreteChoiceModel.selectInjected(selection, injection, random)
    }
}