package edu.kit.ifv.mobitopp.discretechoice.structure


import edu.kit.ifv.mobitopp.discretechoice.models.UtilityAssignment
import edu.kit.ifv.mobitopp.discretechoice.models.UtilityFunction
import edu.kit.ifv.mobitopp.discretechoice.distribution.NestStructure
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.UtilityEnumeration

fun interface UtilityAssignmentBuilder<A, C, P> {

    // TODO add name here?

    fun build(): UtilityAssignment<A, C, P>
}

fun interface UtilityEnumerationBuilder<A, C, P> : UtilityAssignmentBuilder<A, C, P> {

    override fun build(): UtilityEnumeration<A, C, P>
}

interface EnumeratedStructureBuilder<A, C, P> {
    /**
     * Checking whether a situation is equal to a certain element x is a concretization of the more general
     * concept of when to apply a rule.
     */
    fun addUtilityFunctionByIdentifier(option: A, utilityFunction: UtilityFunction<A, C, P>)

    /**
     * Add an option to a nest block via specifying the concrete choice [option] as well as a [utilityFunction] to
     * create a utilityassignment function from the parameters and choice situations.
     */

    fun option(option: A, utilityFunction: P.(A, C) -> Double) {
        val internalUtilityFunction = UtilityFunction { a: A, g: C, p: P ->
            utilityFunction.invoke(p, a, g)
        }

        addUtilityFunctionByIdentifier(option, internalUtilityFunction)
    }

    fun option(option: A, utilityFunction: P.(Pair<A, C>) -> Double) {
        val internalUtilityFunction = UtilityFunction { a: A, c: C, p: P ->
            utilityFunction.invoke(p, a to c)
        }

        addUtilityFunctionByIdentifier(option, internalUtilityFunction)
    }

    /**
     * Add an option to a nest block via specifying the concrete choice [option] as well as a [utilityFunction] to
     * create a utilityassignment function from the parameters and choice situations. Additionally allows a conversion
     * to a different parameter object [T] in case the original parameter object is too verbose/complex
     */
    fun <T> option(option: A, parameters: P.() -> T, utilityFunction: T.(A, C) -> Double) {
        val internalUtilityFunction = UtilityFunction { alternative: A, characteristics: C, parameterObject: P ->
            utilityFunction.invoke(
                parameterObject.parameters(),

                alternative,
                characteristics,
            )
        }
        addUtilityFunctionByIdentifier(option, internalUtilityFunction)
    }

    fun <T> option(option: A, parameters: P.() -> T, utilityFunction: T.(Pair<A, C>) -> Double) {
        val internalUtilityFunction = UtilityFunction { alternative: A, characteristics: C, parameterObject: P ->
            utilityFunction.invoke(
                parameterObject.parameters(),
                alternative to characteristics,
            )
        }
        addUtilityFunctionByIdentifier(option, internalUtilityFunction)
    }


}

/**
 * Convenience function to load a bulk of options with the same utilityassignment function, if the parameter object implements index
 * based lookup of the concrete parameter instantiation that should be used. The options are zipped by appearance (read index)
 * with the corresponding type T from the parameter object. Note that at this point no check can occur whether the index
 * actually exists in the parameter object, as this object is unknown at the creation time of the structure.
 */
fun <T, A, C, P : List<T>> EnumeratedStructureBuilder<A, C, P>.loadFromList(
    options: Collection<A>,
    utilityFunction: T.(A, C) -> Double,
) {
    options.withIndex().forEach { (index, value) ->
        option(value, parameters = { this[index] }, utilityFunction)
    }
}

/**
 * Similarly to [loadFromList] if the parameter object implements the map interface we can trivialize the initialization by simply
 * redirecting the check for the correct parameter implementation by inserting the element into the map.
 */
fun <T, A, C, P : Map<A, T>> EnumeratedStructureBuilder<A, C, P>.loadFromMap(
    options: Collection<A>,
    utilityFunction: T.(A, C) -> Double,
) {
    options.forEach { currentOption ->
        option(currentOption, parameters = { this[currentOption] ?: throw NoSuchElementException("The parameter object $this has no parameter set present for $currentOption, which was requested for utilityassignment calculation. Registered options are ${this.keys}") }, utilityFunction)
    }
}

interface RuleBasedStructureBuilder<A, C, P> : EnumeratedStructureBuilder<A, C, P> {
    fun addUtilityFunctionByRule(rule: (A) -> Boolean, utilityFunction: UtilityFunction<A, C, P>)

    override fun addUtilityFunctionByIdentifier(option: A, utilityFunction: UtilityFunction<A, C, P>) {
        addUtilityFunctionByRule(rule = { it == option }, utilityFunction)
    }

    fun <T> rule(rule: (A) -> Boolean, parameters: P.() -> T, utilityFunction: T.(A) -> Double) {
        val internalUtilityFunction = UtilityFunction { alternative: A, global: C, parameterObject: P ->
            utilityFunction.invoke(
                parameterObject.parameters(),
                alternative
            )
        }
        addUtilityFunctionByRule(rule, internalUtilityFunction)
    }

    fun rule(rule: (A) -> Boolean, utilityFunction: P.(A) -> Double) {
        val internalUtilityFunction = UtilityFunction { alternative: A, global: C, parameterObject: P ->
            utilityFunction.invoke(
                parameterObject,
                alternative
            )
        }
        addUtilityFunctionByRule(rule, internalUtilityFunction)
    }

    fun ruleForAll(utilityFunction: P.(A) -> Double) {
        rule({ true }, utilityFunction)
    }

    fun <T> ruleForAll(parameters: P.() -> T, utilityFunction: T.(A) -> Double) {
        rule({ true }, parameters, utilityFunction)
    }
}



interface NestStructureBuilder<A, P, B> where B : NestStructureBuilder<A, P, B> {

    fun new(): B
    fun children(): List<NestStructure<P>.Node>
    fun addNest(nest: NestStructure<P>.Nest)

    fun nest(name: String, lambda: Double = 1.0, content: B.() -> Unit) =
        nest(name, { lambda }, content)

    fun nest(
        name: String,
        lambdaParameter: P.() -> Double,
        content: B.() -> Unit,
    ): NestStructure<P>.Nest {
        val newBuilder = new()
        newBuilder.content()

        val childNodes = newBuilder.children()
        require(childNodes.isNotEmpty()) {
            "Cannot create an empty nest. You must add at least one option in a nest block using " +
                    "the option(...) { } syntax. This includes the implicit root nest block. "
        }
        val nest = NestStructure<P>().Nest(childNodes, name, lambdaParameter)
        childNodes.forEach { it.parent = nest }
        addNest(nest)
        return nest
    }
}
