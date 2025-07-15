package edu.kit.ifv.mobitopp.discretechoice.utility

import edu.kit.ifv.mobitopp.discretechoice.DiscreteChoiceModel
import edu.kit.ifv.mobitopp.discretechoice.EnumeratedDiscreteChoiceModel
import edu.kit.ifv.mobitopp.discretechoice.distribution.CrossNestedLogit
import edu.kit.ifv.mobitopp.discretechoice.distribution.CrossNestedStructureDataBuilder
import edu.kit.ifv.mobitopp.discretechoice.distribution.MultinomialLogit
import edu.kit.ifv.mobitopp.discretechoice.distribution.NestedLogit
import edu.kit.ifv.mobitopp.discretechoice.distribution.NestedStructureDataBuilder

import edu.kit.ifv.mobitopp.discretechoice.selection.RandomWeightedSelect
import edu.kit.ifv.mobitopp.discretechoice.structure.UtilityAssignmentBuilder
import edu.kit.ifv.mobitopp.discretechoice.structure.UtilityEnumerationBuilder


fun interface DiscreteModelBuilder<A, C, P> {
    fun build(parameters: P): DiscreteChoiceModel<A, C, P>
}

fun interface EnumeratedDiscreteModelBuilder<A, C, P> {
    fun build(parameters: P): EnumeratedDiscreteChoiceModel<A, C, P>
}

fun <R, A, P> UtilityAssignmentBuilder<R, A, P>.openMultinomialLogit(
    name: String,
): DiscreteModelBuilder<R, A, P> =
    DiscreteModelBuilder { parameters ->

        DiscreteChoiceModel(
            name = name,
            utilityAssignment = this.build(),
            distributionFunction = MultinomialLogit(),
            selectionFunction = RandomWeightedSelect(),
            parameters = parameters
        )
    }

fun <R, A, P> UtilityAssignmentBuilder<R, A, P>.multinomialLogit(
    name: String,
    choices: Set<R>,
): EnumeratedDiscreteModelBuilder<R, A, P> =
    EnumeratedDiscreteModelBuilder { parameters ->

        DiscreteChoiceModel(
            name = name,
            utilityAssignment = this.build(),
            distributionFunction = MultinomialLogit(),
            selectionFunction = RandomWeightedSelect(),
            parameters = parameters
        ).with(choices)
    }

fun <R, A, P> UtilityEnumerationBuilder<R, A, P>.multinomialLogit(
    name: String,
): EnumeratedDiscreteModelBuilder<R, A, P> =
    this.multinomialLogit(name, this.build().options)

fun <R, A, P, B> B.nestedLogit(
    name: String,
): EnumeratedDiscreteModelBuilder<R, A, P>
        where B : UtilityEnumerationBuilder<R, A, P>, B : NestedStructureDataBuilder<R, A, P> =
    EnumeratedDiscreteModelBuilder { parameters ->

        val utility = this.build()
        val structure = this.buildStructure()

        DiscreteChoiceModel<R, A, P>(
            name = name,
            utilityAssignment = utility,
            distributionFunction = NestedLogit(structure),
            selectionFunction = RandomWeightedSelect(),
            parameters = parameters
        ).with(utility.options)
    }

fun <A, G, P, B> B.crossNestedLogit(
    name: String,
): EnumeratedDiscreteModelBuilder<A, G, P>
        where
        B : UtilityEnumerationBuilder<A, G, P>,
        B : CrossNestedStructureDataBuilder<A, P> =
    EnumeratedDiscreteModelBuilder { parameters ->

        val utility = this.build()
        val structure = this.buildStructure()

        DiscreteChoiceModel(
            name = name,
            utilityAssignment = utility,
            distributionFunction = CrossNestedLogit(structure),
            selectionFunction = RandomWeightedSelect(),
            parameters = parameters
        ).with(utility.options)
    }
