import discreteChoice.DiscreteChoiceModel
import discreteChoice.models.ChoiceAlternative
import discreteChoice.structure.CrossNestedStructure
import discreteChoice.structure.DiscreteStructure
import discreteChoice.utility.crossNestedLogit
import discreteChoice.utility.multinomialLogit
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * I would really like to be able to test changes to the discrete choice modelling with a performance output.
 */

private class ExampleChoice(override val choice: Int) : ChoiceAlternative<Int>() {

}

fun main() {
    val optionSize = 10000
    val iterations = 10000
    val random = Random(1L)

    val randomUtilityFunction: (alternative: ExampleChoice, parameterObject: Unit) -> Double = { _, _ ->
        random.nextDouble(-10.0, 10.0)
    }


    val discreteChoiceModel = DiscreteStructure<Int, ExampleChoice, Unit> {
        for(i in 1..optionSize) {
            addUtilityFunctionByIdentifier(i, randomUtilityFunction)
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