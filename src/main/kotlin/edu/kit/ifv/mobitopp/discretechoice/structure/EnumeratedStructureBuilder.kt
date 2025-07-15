package edu.kit.ifv.mobitopp.discretechoice.structure


import edu.kit.ifv.mobitopp.discretechoice.UtilityAssignment
import edu.kit.ifv.mobitopp.discretechoice.UtilityFunction
import edu.kit.ifv.mobitopp.discretechoice.distribution.NestStructure
import edu.kit.ifv.mobitopp.discretechoice.utility.UtilityEnumeration

fun interface UtilityAssignmentBuilder<R, A, P> {

    // TODO add name here?

    fun build(): UtilityAssignment<R, A, P>
}

fun interface UtilityEnumerationBuilder<R, A, P> : UtilityAssignmentBuilder<R, A, P> {

    override fun build(): UtilityEnumeration<R, A, P>
}

data class CollectTheStuff<A, G>(
    val global: G,
    val choice: A,
)

interface EnumeratedStructureBuilder<A, G, P> {
    /**
     * Checking whether a situation is equal to a certain element x is a concretization of the more general
     * concept of when to apply a rule.
     */
    fun addUtilityFunctionByIdentifier(option: A, utilityFunction: UtilityFunction<A, G, P>)

    /**
     * Add an option to a nest block via specifying the concrete choice [option] as well as a [utilityFunction] to
     * create a utility function from the parameters and choice situations.
     */

    fun option(option: A, utilityFunction: P.(CollectTheStuff<A, G>) -> Double) {

        val internalUtilityFunction: UtilityFunction<A, G, P> = UtilityFunction {

                alternative: A, global: G, parameters: P ->
            utilityFunction.invoke(parameters, CollectTheStuff(global, alternative))


        }
        addUtilityFunctionByIdentifier(option, internalUtilityFunction)
    }

    fun option(option: A, utilityFunction: P.(A, G) -> Double) {
        val internalUtilityFunction = UtilityFunction { a: A, g: G, p: P ->
            utilityFunction.invoke(p, a, g)
        }

        addUtilityFunctionByIdentifier(option, internalUtilityFunction)
    }

    /**
     * Add an option to a nest block via specifying the concrete choice [option] as well as a [utilityFunction] to
     * create a utility function from the parameters and choice situations. Additionally allows a conversion
     * to a different parameter object [T] in case the original parameter object is too verbose/complex
     */
    fun <T> option(option: A, parameters: P.() -> T, utilityFunction: T.(A) -> Double) {
        val internalUtilityFunction = UtilityFunction { alternative: A, global: G, parameterObject: P ->
            utilityFunction.invoke(
                parameterObject.parameters(),
                alternative
            )
        }
        addUtilityFunctionByIdentifier(option, internalUtilityFunction)
    }


}

/**
 * Convenience function to load a bulk of options with the same utility function, if the parameter object implements index
 * based lookup of the concrete parameter instantiation that should be used. The options are zipped by appearance (read index)
 * with the corresponding type T from the parameter object. Note that at this point no check can occur whether the index
 * actually exists in the parameter object, as this object is unknown at the creation time of the structure.
 */
fun <T, R, A, P : List<T>> EnumeratedStructureBuilder<R, A, P>.bulkList(
    options: Collection<R>,
    utilityFunction: T.(R) -> Double,
) {
    options.withIndex().forEach { (index, value) ->
        option(value, parameters = { this[index] }, utilityFunction)
    }
}

/**
 * Similarly to [bulkList] if the parameter object implements the map interface we can trivialize the initialization by simply
 * redirecting the check for the correct parameter implementation by inserting the element R
 */
fun <T, R, A, P : Map<R, T>> EnumeratedStructureBuilder<R, A, P>.bulkMap(
    options: Collection<R>,
    utilityFunction: T.(R) -> Double,
) {
    options.forEach { currentOption ->
        option(currentOption, parameters = { this.getValue(currentOption) }, utilityFunction)
    }
}

interface RuleBasedStructureBuilder<A, G, P> : EnumeratedStructureBuilder<A, G, P> {
    fun addUtilityFunctionByRule(rule: (A) -> Boolean, utilityFunction: UtilityFunction<A, G, P>)

    override fun addUtilityFunctionByIdentifier(option: A, utilityFunction: UtilityFunction<A, G, P>) {
        addUtilityFunctionByRule(rule = { it == option }, utilityFunction)
    }

    fun <T> rule(rule: (A) -> Boolean, parameters: P.() -> T, utilityFunction: T.(A) -> Double) {
        val internalUtilityFunction = UtilityFunction { alternative: A, global: G, parameterObject: P ->
            utilityFunction.invoke(
                parameterObject.parameters(),
                alternative
            )
        }
        addUtilityFunctionByRule(rule, internalUtilityFunction)
    }

    fun rule(rule: (A) -> Boolean, utilityFunction: P.(A) -> Double) {
        val internalUtilityFunction = UtilityFunction { alternative: A, global: G, parameterObject: P ->
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

/**
 * These functions should reside in the package where utility functions are built, so that they are available
 * whereever someone creates a utility function, without needing to import.
 */
inline val Boolean.D get() = if (this) 1.0 else 0.0
operator fun Boolean.times(double: Double): Double {
    return this.D * double
}

interface NestStructureBuilder<R, P, B> where B : NestStructureBuilder<R, P, B> {

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
