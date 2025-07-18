package edu.kit.ifv.mobitopp.discretechoice.structure


import edu.kit.ifv.mobitopp.discretechoice.models.UtilityFunction
import edu.kit.ifv.mobitopp.discretechoice.distribution.CrossNestStructureData
import edu.kit.ifv.mobitopp.discretechoice.distribution.CrossNestedStructureDataBuilder
import edu.kit.ifv.mobitopp.discretechoice.distribution.NestStructure
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.MapBasedUtilityEnumeration
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.UtilityEnumeration

class CrossNestedStructure<A, C, P>(
    content: CrossNestedDAG<A, P>.() -> Unit,
) : CrossNestedStructureDataBuilder<A, P> {

    private val leafsByOption: MutableMap<A, MutableList<NestStructure<P>.Leaf>> = mutableMapOf()
    private val root: NestStructure<P>.Nest =
        CrossNestedDAG<A, P>(leafsByOption).nest("root") {
            content()
        }

    override fun buildStructure() = CrossNestStructureData<A, P>(root, leafsByOption)

    fun withUtility(
        lambda: EnumeratedStructureBuilder<A, C, P>.() -> Unit,
    ) = CrossNestedWithUtilities<A, C, P>(this, lambda)
}

class CrossNestedDAG<A, P>(
    val leafsByOption: MutableMap<A, MutableList<NestStructure<P>.Leaf>>,
) : NestStructureBuilder<A, P, CrossNestedDAG<A, P>> {

    private val children: MutableList<NestStructure<P>.Node> = mutableListOf()

    override fun new(): CrossNestedDAG<A, P> = CrossNestedDAG<A, P>(leafsByOption = leafsByOption)
    override fun children(): List<NestStructure<P>.Node> = children
    override fun addNest(nest: NestStructure<P>.Nest) {
        children.add(nest)
    }

    fun option(option: A, name: String = option.toString(), alpha: Double = 1.0) =
        option(option, name) { alpha }

    fun option(option: A, name: String = option.toString(), alpha: P.() -> Double): NestStructure<P>.Leaf {
        val element = NestStructure<P>().Leaf(extractAlphaParameter = alpha, name = name)
        children.add(element)
        val globalEntries = leafsByOption.getOrPut(option) {
            mutableListOf()
        }
        globalEntries.add(element)
        return element
    }
}

class CrossNestedWithUtilities<A, C, P>(
    private val structure: CrossNestedStructure<A, C, P>,
    lambda: EnumeratedStructureBuilder<A, C, P>.() -> Unit,
) : EnumeratedStructureBuilder<A, C, P>,
    UtilityEnumerationBuilder<A, C, P>,
    CrossNestedStructureDataBuilder<A, P> by structure {

    private var utilityFunctions: MutableMap<A, UtilityFunction<A, C, P>> = mutableMapOf()

    init {
        lambda()
    }

    override fun addUtilityFunctionByIdentifier(
        option: A,
        utilityFunction: UtilityFunction<A, C, P>,
    ) {
        require(!utilityFunctions.containsKey(option)) {
            "Duplicate: A utilityassignment function for $option has already been defined in this structure. " +
                    "Current elements ${utilityFunctions.keys} already have a utilityassignment function associated. "
        }
        utilityFunctions[option] = utilityFunction
    }

    override fun build(): UtilityEnumeration<A, C, P> = MapBasedUtilityEnumeration<A, C, P>(utilityFunctions)
}
