package benchmark

import discreteChoice.distribution.MultinomialLogit
import discreteChoice.incubator.GDiscreteChoiceModel
import discreteChoice.incubator.GFUtilityFunction
import discreteChoice.selection.RandomWeightedSelect
import discreteChoice.structure.DiscreteStructure
import discreteChoice.utility.GMapBasedUtilityEnumeration
import discreteChoice.utility.multinomialLogit
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

    val otherDiscreteChoiceModel = GDiscreteChoiceModel<Int, Unit, Unit>(
        name = "test",
        utilityAssignment = GMapBasedUtilityEnumeration((1..10).associateWith {
            GFUtilityFunction { _,_,_ ->
                random.nextDouble(
                    -10.0,
                    10.0
                )
            }
        }),
        distributionFunction = MultinomialLogit(),
        selectionFunction = RandomWeightedSelect(),
        parameters = Unit
    )

    val choices = (1..10).toSet()
    @Benchmark
    fun newChoiceModel(blackhole: Blackhole) {


        val selection = context(Unit, random) {
            otherDiscreteChoiceModel.select(choices)
        }
        blackhole.consume(selection)
    }

    private val a = 42
    private val b = 58
    private val pair = Pair(a, b)

//    @Benchmark
    fun callWithPair(): Int {
        return method(pair)
    }

//    @Benchmark
    fun callWithSeparateArgs(): Int {
        return method(a, b)
    }

    private fun method(pair: Pair<Int, Int>): Int = pair.first + pair.second
    private fun method(a: Int, b: Int): Int = a + b
}

fun main() {
    val choiceModel = BenchmarkRun().otherDiscreteChoiceModel
    context(Unit, Random(1)) {
        choiceModel.select(setOf(1, 2, 3, 4))   }
}