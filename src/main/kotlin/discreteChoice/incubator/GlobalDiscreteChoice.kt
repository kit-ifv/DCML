package discreteChoice.incubator

import discreteChoice.DistributionFunction
import discreteChoice.SelectionFunction
import discreteChoice.UtilityAssignment
import discreteChoice.distribution.MultinomialLogit
import discreteChoice.selection.RandomWeightedSelect
import discreteChoice.structure.DiscreteStructure
import discreteChoice.utility.associateWithNotNull
import kotlin.random.Random


interface GChoiceModel<A, G> {
    context(global: G)
    fun select(choices: Collection<A>, random: Random): A
}

fun interface GFUtilityFunction<A, G, P> {
    fun calculateUtility(alternative: A, constants: G, parameterObject: P) : Double
}

fun interface GUtilityAssignment<A, G, P> {
    fun getUtilityFunctionFor(alternative: A): GFUtilityFunction<A, G, P>
    operator fun get(alternative: A) = getUtilityFunctionFor(alternative)
}

data class GDiscreteChoiceModel<A, G, P>(
    val name: String,
    val utilityAssignment: GUtilityAssignment<A, G, P>,
    val distributionFunction: DistributionFunction<A, P>,
    val selectionFunction: SelectionFunction<A>,
    val parameters: P
) : GChoiceModel<A, G> {
    context(global: G)
    override fun select(choices: Collection<A>, random: Random): A {
        return selectionFunction.calculateSelection(probabilities(choices), random)
    }
    context(global: G)
    fun utilities(alternatives: Collection<A>): Map<A, Double> {
        return alternatives.associateWithNotNull { utility(it) }
    }
    context(global: G)
    fun utility(alternative: A): Double {
        return utilityAssignment[alternative].calculateUtility(alternative, global, parameters)
    }
    context(global: G)
    fun probabilities(alternatives: Collection<A>): Map<A, Double> {
        return distributionFunction.calculateProbabilities(utilities(alternatives), parameters)
    }

}

context(parameter: Double, aloha: String)
fun funny() {
    val x = parameter - 1.0
    println("$aloha $x")
}

fun main() {
    val model = GDiscreteChoiceModel<Int, String, Unit>(
        name = "test",
        utilityAssignment = GUtilityAssignment { GFUtilityFunction{a, g, p ->
            (g.length * a).toDouble() / 4
        }},
        distributionFunction = MultinomialLogit(),
        selectionFunction = RandomWeightedSelect(),
        parameters = Unit
    )


    val probabilities = with("Sparta") {
        model.probabilities(setOf(1, 2, 3))
    }
    with("Context") {
        model.select(setOf(1, 2, 3), Random(1))
    }


    println(probabilities)
}