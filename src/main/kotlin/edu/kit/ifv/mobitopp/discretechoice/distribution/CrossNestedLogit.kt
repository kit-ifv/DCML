package edu.kit.ifv.mobitopp.discretechoice.distribution

import edu.kit.ifv.mobitopp.discretechoice.distribution.DistributionFunction


class CrossNestedLogit<A, P>(
    private val structure: CrossNestStructureData<A, P>,
) : DistributionFunction<A, P> {

    override fun calculateProbabilities(utilities: Map<A, Double>, parameters: P): Map<A, Double> {
        require(utilities.isNotEmpty()) {
            "Received empty utilityassignment map!"
        }

        val root = structure.root
        val leafs = structure.leafs

        return synchronized(root) {
            root.reset()

            val situations = utilities.entries.flatMap { (k, v) ->
                leafs[k]?.map { AssociatedSituation(k, it, v) } ?: emptyList()
            }
            val crossNestedSimilarity = situations.groupBy { it.sit }.values.associateWith {
                it.sumOf { sit ->
                    sit.leaf.extractAlphaParameter(parameters)
                }
            }

            require(crossNestedSimilarity.none { it.value != 1.0 }) {
                println(
                    "Your alpha parameters do not sum to 1 for the alternatives ${
                        crossNestedSimilarity.filter { it.value != 1.0 }
                            .map { "${it.key.first().sit} ${it.value}" }
                    }"
                )
            }

            runQueue(situations, parameters)
            situations.groupBy { it.sit }.mapValues { it.value.sumOf { it.probability } }
        }
    }
}

/**
 * The nest structure.
 */
data class CrossNestStructureData<A, P>(
    val root: NestStructure<P>.Node,
    val leafs: Map<A, List<NestStructure<P>.Leaf>>,
)


/**
 * Functional interface for conditional spawning of CrossNestedStructureData.
 */
fun interface CrossNestedStructureDataBuilder<A, P> {
    fun buildStructure(): CrossNestStructureData<A, P>
}