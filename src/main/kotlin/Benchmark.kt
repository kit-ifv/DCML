import discreteChoice.models.ChoiceAlternative
import discreteChoice.structure.DiscreteStructure
import discreteChoice.utility.multinomialLogit
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * I would really like to be able to test changes to the discrete choice modelling with a performance output.
 */

private class ExampleChoice(override val choice: Int) : ChoiceAlternative<Int>() {

}

/**
 * This simple script can be used to run a benchmark to test the efficiecy of discrete choice models.
 */
fun main() {
    val optionSize = 10000
    val iterations = 10000
    val random = Random(1L)

    val discreteChoiceModel = DiscreteStructure<Int, ExampleChoice, Unit> {
        for(i in 1..optionSize) {
            option(i) {
                random.nextDouble(-10.0, 10.0)
            }
        }

    }.multinomialLogit("Benchmark model").build(Unit).model
    val options = (1..optionSize).map { ExampleChoice(it) }.toSet()
    val timer = measureTimeMillis {
        repeat(iterations) {
            discreteChoiceModel.select(options, random)
        }
    }

    println("Running timing test took $timer ms")




}