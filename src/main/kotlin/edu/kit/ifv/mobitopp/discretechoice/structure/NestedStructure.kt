package edu.kit.ifv.mobitopp.discretechoice.structure

import edu.kit.ifv.mobitopp.discretechoice.UtilityFunction
import edu.kit.ifv.mobitopp.discretechoice.distribution.NestStructure
import edu.kit.ifv.mobitopp.discretechoice.distribution.NestStructureData
import edu.kit.ifv.mobitopp.discretechoice.distribution.NestedStructureDataBuilder
import edu.kit.ifv.mobitopp.discretechoice.utility.MapBasedUtilityEnumeration
import edu.kit.ifv.mobitopp.discretechoice.utility.UtilityEnumeration


class NestedStructure<A, C, P>(

    lambda: NestedTree<A, C, P>.() -> Unit,
) :
    UtilityEnumerationBuilder<A, C, P>,
    NestedStructureDataBuilder<A, C, P> {

    private val leafs: MutableMap<A, NestStructure<P>.Leaf> = mutableMapOf()
    private val utilityFunctions: MutableMap<A, UtilityFunction<A, C, P>> = mutableMapOf()
    private val root: NestStructure<P>.Nest =
        NestedTree<A, C, P>(leafs, utilityFunctions).nest("root") {
            lambda()
        }

    override fun build(): UtilityEnumeration<A, C, P> = MapBasedUtilityEnumeration<A, C, P>(utilityFunctions)

    override fun buildStructure() = NestStructureData<A, C, P>(root, leafs.toMap())
}

class NestedTree<A, G, P>(
    private val leafs: MutableMap<A, NestStructure<P>.Leaf>,
    private val utilityFunctions: MutableMap<A, UtilityFunction<A, G, P>>,
) : EnumeratedStructureBuilder<A, G, P>,
    NestStructureBuilder<A, P, NestedTree<A, G, P>> {

    private val children: MutableList<NestStructure<P>.Node> = mutableListOf()

    override fun new(): NestedTree<A, G, P> = NestedTree<A, G, P>(leafs, utilityFunctions)
    override fun children(): List<NestStructure<P>.Node> = children
    override fun addNest(nest: NestStructure<P>.Nest) {
        children.add(nest)
    }

    override fun addUtilityFunctionByIdentifier(option: A, utilityFunction: UtilityFunction<A, G, P>) {
        utilityFunctions[option] = utilityFunction
        val element = NestStructure<P>().Leaf()
        children.add(element)
        require(!leafs.containsKey(option)) {
            "A utility function for $option has already been defined in this nest structure. Current elements" +
                    " ${leafs.keys} have a utility function associated. "
        }
        leafs[option] = element
    }
}
