package edu.kit.ifv.mobitopp.discretechoice.distribution


import edu.kit.ifv.mobitopp.discretechoice.DistributionFunction
import edu.kit.ifv.mobitopp.discretechoice.utility.printAsTree
import java.util.PriorityQueue
import kotlin.math.exp
import kotlin.math.ln

/**
 * DistributionFunction which calculates distributions
 */
class NestedLogit<R, A, P>(
    private val structure: NestStructureData<R, A, P>,
) : DistributionFunction<R, P> {

    /**
     * @param utilities Should only map choices, which are present in the `structure` of this NestedLogit. Will fail
     * if other options are mapped. Only mapped options get included in the calculation of probabilities.
     */
    override fun calculateProbabilities(utilities: Map<R, Double>, parameters: P): Map<R, Double> {
        val (root, leafs) = structure

        return synchronized(root) {
            root.reset() // Reset the calculation tree to reset the relevantForCalculation flags.
            val relevantLeafs = utilities.entries.map {
                AssociatedSituation(
                    it.key,
                    leafs[it.key]!!,
                    it.value
                )
            }
            runQueue(relevantLeafs, parameters) // calculate probabilities of entire graph
            // (only the relevant nodes included)
            relevantLeafs.associate { it.sit to it.probability } // extract the probabilities of the options out of the
            // leafs.
        }
    }
}

data class NestStructureData<R, A, P>(
    val root: NestStructure<P>.Node,
    val leafs: Map<R, NestStructure<P>.Leaf>,
)

fun interface NestedStructureDataBuilder<R, A, P> {
    fun buildStructure(): NestStructureData<R, A, P>
}

// TODO can we implement this stateless?
/**
 * Calculates the probabilities of all options, given the leaves to include (the `situations`).
 * @param situations basically a list of leafs of the structure, which should be included in calculating the
 * probability values of all nodes of the underlying structure.
 */
fun <A, P> runQueue(
    situations: List<AssociatedSituation<A, P>>,
    parameters: P,
) {
    val nextNests = situations.mapNotNull { it.initializeUtility() }
    // Keep nodes sorted by increasing level.
    val queue = PriorityQueue<NestStructure<P>.Nest> { a, b -> a.level - b.level }

    lateinit var lastElement: NestStructure<P>.Nest
    queue.addAll(nextNests)
    // calculate all utilities bottom up. So lowest levels first, then higher ones.
    while (queue.isNotEmpty()) {
        val n = queue.poll()
        lastElement = n
        val parent = n.calculateUtility(parameters) // bottom up calculate the utilites of all nodes involved in this
        // calculation.
        parent?.let { queue.add(it) }
    }
    lastElement.probability = 1.0 // top most node has 1.0 probability as this is all the probability which should be
    // distributed across the options.
    lastElement.calculateProbability(parameters) // assign probability values to all nodes of the structure.
}

/**
 * We need to cross-reference an arbitrary situation [A] to the corresponding [leaf]. This class maintains
 * this object state until we release the probability calculation
 */
class AssociatedSituation<A, P>(
    val sit: A,
    val leaf: NestStructure<P>.Leaf,
    val utility: Double,
) {
    val probability get() = leaf.probability

    /**
     * Set the utility of the leaf to this.utility and marks it and the parent as "relevantForTheCalculation".
     */
    fun initializeUtility(): NestStructure<P>.Nest? {
        return leaf.initializeUtility(utility)
    }
}

class NestStructure<P> {

    fun build(leafs: Set<Leaf>) {
        leafs.groupBy { it.parent }
    }

    /**
     * A generic Node of the structure graph. Defines all values and functions any Node in a NestStructure needs to
     * implement.
     * @property extractAlphaParameter Provider function for a tuning parameter of the softmax used to distribute the
     * probabilities. The returned value will be used as a factor for the probability calculation. $\alpha  e^{u_i}$...
     */
    abstract inner class Node {
        abstract val extractAlphaParameter: (P) -> Double

        /**
         * Level represents the depth of the alternative in the Nest Structure, lower level nests need to
         * be calculated for their utility first.
         */
        abstract val level: Int

        /**
         * If set to false, this node will get ignored in utility and probability calculations.
         */
        var relevantForCalculation = false
        abstract var parent: Nest?
        abstract fun reset()
        var utility: Double = 0.0
        var probability: Double = 0.0
        abstract val name: String
        open fun calculateProbability(parameters: P) {
        }

        fun treePrint(): Unit = printAsTree(
            root = this,
            label = { it.toString() },
            getChildren = { it.children() }
        )

        abstract fun children(): List<Node>
        abstract fun leafs(): List<Leaf>
    }

    /**
     * A non-abstract Node __without__ children.
     */
    inner class Leaf(
        override val extractAlphaParameter: (P) -> Double = {
            1.0
        },
        override val name: String = hashCode().toString(),
    ) : Node() {
        override var parent: Nest? = null
        override val level: Int = 0

        override fun reset() {
            relevantForCalculation = false
        }

        /**
         * Set utility of this leaf and mark this leaf and this.parent as relevant for the calculation.
         * @return the Parent of this Leaf.
         */
        fun initializeUtility(utility: Double): Nest? {
            this.utility = utility
            relevantForCalculation = true
            parent?.relevantForCalculation = true
            return parent
        }

        override fun toString(): String {
            return "Leaf $name: [U: $utility, P: $probability]"
        }

        override fun children(): List<NestStructure<P>.Node> = emptyList()
        override fun leafs(): List<NestStructure<P>.Leaf> = listOf(this)
    }

    /**
     * A non-abstract Node __with__ children.
     * @param extractLambdaParameter provider function for tuning parameter of the softmax. Lambda defines how much
     * probability should be given to the highest utility and how much to lower utility values.
     *
     * lambda = 1 is the normal softmax.
     *
     * lambda > 1 Means more even distribution of probability.
     *
     * lambda < 1 leads to more probability given to the option with the highest utility value.
     *
     * __Lambda has to always be > 0.__
     */
    inner class Nest(
        private val childNodes: Collection<Node>,
        override val name: String = hashCode().toString(),
        val extractLambdaParameter: (P) -> Double,
    ) :
        Node() {
        override var parent: Nest? = null
        override val level = childNodes.maxOf { it.level } + 1

        /**
         * @property maxUtility keep track of the highest utility found in the children, to subtract that value from
         * each utility calculation, to turn big utilities to small numbers, and numeric problems with exp and ln()
         * from a potential infinity to a 0.0 which is better handleable.
         */
        private var maxUtility = 0.0

        /**
         * Sum of all exp(utility) of the utility of all children.
         */
        private var sum = 0.0

        override val extractAlphaParameter: (P) -> Double = {
            1.0
        } // Alpha parameter is only relevant for leaves. and thus not in the constructor

        /**
         * Sets `relevantForCalculation` to false. And calls `reset` on all children.
         */
        override fun reset() {
            relevantForCalculation = false
            childNodes.forEach { it.reset() }
        }

        /**
         * Calculates the utility of this node. Sets `this.sum`, `this.utility` and `this.maxUtility`
         * @return the parent of this node.
         */
        fun calculateUtility(parameters: P): Nest? {
            val lambda = extractLambdaParameter(parameters)
            val relevantChilds = childNodes.filter { it.relevantForCalculation }
            if (relevantChilds.isEmpty()) {
                error("Never should a calculate Utility be called when the children are irrelevant")
            }
            maxUtility = relevantChilds.maxOf { ln(it.extractAlphaParameter(parameters)) + it.utility }
            val x = relevantChilds
                .map { ln(it.extractAlphaParameter(parameters)) + it.utility }
                .sumOf { exp((it - maxUtility) / lambda) }

            utility = maxUtility + lambda * ln(x) // this utility is the sum of the utility of the children. The
            // maxUtility and lambda get canceled out in later calculations.
            sum = x
            return parent
        }

        /**
         * Assigns probabilities to __all__ children of this node. (The probability of this node will get distributed
         * on the children of this node).
         *
         * All probabilities of the (relevantForCalculation) children will sum up to `this.probability`.
         *
         * Probability of the children get distributed using the (lambda tuned) softmax of their utility values.
         */
        override fun calculateProbability(parameters: P) {
            val lambda = extractLambdaParameter(parameters)
            val relevantChilds = childNodes.filter { it.relevantForCalculation }
            relevantChilds.forEach {
                val actualUtil = ln(it.extractAlphaParameter(parameters)) + it.utility
                val utilCalculation = actualUtil.let { d -> exp((d - maxUtility) / lambda) / sum }
                val childProbability =
                    this.probability * utilCalculation
                it.probability = childProbability
            }
            relevantChilds.forEach { it.calculateProbability(parameters) }
        }

        override fun children(): List<Node> = childNodes.toList()

        override fun leafs(): List<NestStructure<P>.Leaf> = childNodes.flatMap { it.leafs() }

        override fun toString(): String {
            return "$name: (U: $utility P: $probability): Childs: ${
                childNodes.joinToString(
                    prefix = "[",
                    postfix = "]"
                ) { it.name }
            }"
        }
    }
}
