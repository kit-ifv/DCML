package benchmark

import edu.kit.ifv.mobitopp.discretechoice.structure.DiscreteStructure
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.multinomialLogit
import org.openjdk.jmh.infra.Blackhole
import kotlin.random.Random


fun main() {
    val random = Random(1L)
    val discreteChoiceModel = DiscreteStructure<Int, Unit, Unit> {
        for (i in 1..10000) {
            option(i) {
                random.nextDouble(-10.0, 10.0)
            }
        }

    }.multinomialLogit("Benchmark model").build(Unit)

    val output = context(Unit, random) {
        (1..10000).map { discreteChoiceModel.select() }
    }
    println(output)
}