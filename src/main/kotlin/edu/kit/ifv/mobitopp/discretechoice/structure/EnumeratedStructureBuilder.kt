package edu.kit.ifv.mobitopp.discretechoice.structure


import edu.kit.ifv.mobitopp.discretechoice.distribution.NestStructure
import edu.kit.ifv.mobitopp.discretechoice.models.UtilityFunction
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.UtilityAssignment
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.UtilityEnumeration

/**
 * Builder that can produce a [UtilityAssignment].
 *
 * Implementations collect the information necessary to create a [UtilityAssignment] and
 * expose a single terminal operation [build] that returns the built instance.
 *
 * @param A the type representing an option/alternative.
 * @param C the type of context specific parameters available to utility functions.
 * @param P the type of any additional parameters used by utility functions.
 */
fun interface UtilityAssignmentBuilder<in A, in C, in P> {

    // TODO add name here?
    /**
     * Build and return a [UtilityAssignment] described by this builder.
     *
     * @return a [UtilityAssignment] configured according to the builder state.
     */
    fun build(): UtilityAssignment<A, C, P>
}

/**
 * Builder that produces a [UtilityEnumeration], a specialization of [UtilityAssignment]
 * that enumerates a discrete set of alternatives.
 *
 * This functional interface narrows the return type of [build] to [UtilityEnumeration].
 *
 * @param A the type representing an option/alternative.
 * @param C the type of context specific parameters available to utility functions.
 * @param P the type of any additional parameters used by utility functions.
 */
fun interface UtilityEnumerationBuilder<A, in C, in P> : UtilityAssignmentBuilder<A, C, P> {
    /**
     * Build and return a [UtilityEnumeration] described by this builder.
     *
     * @return a [UtilityEnumeration] configured according to the builder state.
     */
    override fun build(): UtilityEnumeration<A, C, P>
}

/**
 * Builder API for enumerated (discrete) structures of alternatives.
 *
 * Implementations allow registering utility functions for specific alternatives (options).
 * Several convenience overloads are provided to register utility functions using different
 * forms of parameter and context access.
 *
 * @param A the type representing an option/alternative.
 * @param C the type of context specific parameters available to utility functions.
 * @param P the type of any additional parameters used by utility functions.
 */
interface EnumeratedStructureBuilder<A, C, P> {
    /**
     * Register a utility function for the given concrete [option].
     *
     * Implementations are expected to record the association between [option] and
     * [utilityFunction] so that it can later be used by a [UtilityEnumeration] or
     * other consumer.
     *
     * @param option the specific alternative to associate the function with.
     * @param utilityFunction the utility function that computes a utility for this [option].
     */
    fun addUtilityFunctionByIdentifier(option: A, utilityFunction: UtilityFunction<A, C, P>)

    /**
     * Convenience: add an option by providing a receiver-style utility function on [P].
     *
     * The provided [utilityFunction] is a lambda with receiver [P] that accepts separate
     * parameters (alternative and context). It is wrapped into an internal [UtilityFunction]
     * before registration.
     *
     * @param option the specific alternative to register.
     * @param utilityFunction receiver-style function: `P.(A, C) -> Double`.
     */
    fun option(option: A, utilityFunction: P.(A, C) -> Double) {
        val internalUtilityFunction = UtilityFunction { a: A, g: C, p: P ->
            utilityFunction.invoke(p, a, g)
        }

        addUtilityFunctionByIdentifier(option, internalUtilityFunction)
    }

    /**
     * Convenience: add an option by providing a receiver-style utility function on [P]
     * that accepts a Pair of alternative and context.
     *
     * @param option the specific alternative to register.
     * @param utilityFunction receiver-style function: `P.(Pair<A, C>) -> Double`.
     */
    fun option(option: A, utilityFunction: P.(Pair<A, C>) -> Double) {
        val internalUtilityFunction = UtilityFunction { a: A, c: C, p: P ->
            utilityFunction.invoke(p, a to c)
        }

        addUtilityFunctionByIdentifier(option, internalUtilityFunction)
    }

    /**
     * Convenience: add an option while first transforming the parameter object [P] into
     * a custom parameter object of type [T], then applying a utility function on [T].
     *
     * This is useful when the raw parameter type [P] is verbose or requires adaptation.
     *
     * @param T the derived parameter type produced by [parameters].
     * @param option the specific alternative to register.
     * @param parameters a conversion function transforming `P` into `T`.
     * @param utilityFunction receiver-style function on `T`: `T.(A, C) -> Double`.
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

    /**
     * Convenience: add an option while transforming [P] into [T] and using a utility function
     * that accepts a Pair of alternative and context.
     *
     * @param T the derived parameter type produced by [parameters].
     * @param option the specific alternative to register.
     * @param parameters a conversion function transforming `P` into `T`.
     * @param utilityFunction receiver-style function on `T`: `T.(Pair<A, C>) -> Double`.
     */
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
 * Even if there is no recognizable structure like a list or map in [P] we can still load multiple options, but they
 * are using the global parameter object in that case.
 */
fun <A, C, P> EnumeratedStructureBuilder<A, C, P>.forOptions(
    elements: Iterable<A>,
    utilityFunction: P.(A, C) -> Double,
) {
    elements.forEach {
        option(it, utilityFunction)
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
        option(
            currentOption,
            parameters = {
                this[currentOption]
                    ?: throw NoSuchElementException("The parameter object $this has no parameter set present for $currentOption, which was requested for utilityassignment calculation. Registered options are ${this.keys}")
            },
            utilityFunction
        )
    }
}

interface RuleBasedStructureBuilder<A, C, P> : EnumeratedStructureBuilder<A, C, P> {
    fun addUtilityFunctionByRule(rule: (A) -> Boolean, utilityFunction: UtilityFunction<A, C, P>)

    override fun addUtilityFunctionByIdentifier(option: A, utilityFunction: UtilityFunction<A, C, P>) {
        addUtilityFunctionByRule(rule = { it == option }, utilityFunction)
    }

    fun <T> rule(rule: (A) -> Boolean, parameters: P.() -> T, utilityFunction: T.(A, C) -> Double) {
        val internalUtilityFunction = UtilityFunction { alternative: A, global: C, parameterObject: P ->
            utilityFunction.invoke(
                parameterObject.parameters(),
                alternative, global,
            )
        }
        addUtilityFunctionByRule(rule, internalUtilityFunction)
    }

    fun rule(rule: (A) -> Boolean, utilityFunction: P.(A, C) -> Double) {
        val internalUtilityFunction = UtilityFunction { alternative: A, global: C, parameterObject: P ->
            utilityFunction.invoke(
                parameterObject,

                alternative,
                global,
            )
        }
        addUtilityFunctionByRule(rule, internalUtilityFunction)
    }

    fun ruleForAll(utilityFunction: P.(A, C) -> Double) {
        rule({ true }, utilityFunction)
    }

    fun <T> ruleForAll(parameters: P.() -> T, utilityFunction: T.(A, C) -> Double) {
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
