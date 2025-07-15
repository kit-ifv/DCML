package edu.kit.ifv.mobitopp.discretechoice.selection


import edu.kit.ifv.mobitopp.discretechoice.SelectionFunction
import edu.kit.ifv.mobitopp.discretechoice.utility.select
import kotlin.random.Random

/**
 * A SelectionFunction, which selects one of the options based on their assigned probability.
 */
class RandomWeightedSelect<X> : SelectionFunction<X> {
    override fun calculateSelection(options: Map<X, Double>, random: Random): X = options.select(random.nextDouble())
}

/**
 * A SelectionFunction, which selects one of the options without regarding the assigned probabilities. Acts like every
 * option has the same probability.
 */
class UniformSelect<X> : SelectionFunction<X> {
    override fun calculateSelection(options: Map<X, Double>, random: Random): X {
        val index = random.nextInt(options.size)
        return options.keys.toList()[index]
    }
}
