package benchmark


import edu.kit.ifv.mobitopp.discretechoice.structure.DiscreteStructure
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.multinomialLogit
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class Paramters(val asc_d: Double = 0.20)


@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
open class BenchmarkRun {
    val random = Random(1L)
    val discreteChoiceModel = DiscreteStructure<Int, Unit, Unit> {
        for (i in 1..10) {
            option(i) {
                random.nextDouble(-10.0, 10.0)
            }
        }

    }.multinomialLogit("Benchmark model").build(Unit)


    val choices = (1..10).toSet()

    @Benchmark
    fun newChoiceModel(blackhole: Blackhole) {


        val selection = context(Unit, random) {
            discreteChoiceModel.select(choices)
        }
        blackhole.consume(selection)
    }
}
