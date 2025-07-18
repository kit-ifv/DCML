package edu.kit.ifv.mobitopp.discretechoice.utilityassignment

import edu.kit.ifv.mobitopp.discretechoice.models.DiscreteChoiceModel
import edu.kit.ifv.mobitopp.discretechoice.models.EnumeratedDiscreteChoiceModel
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

fun <A, C, P> UtilityAssignmentBuilder<A, C, P>.openMultinomialLogit(
    name: String,
): DiscreteModelBuilder<A, C, P> =
    DiscreteModelBuilder { parameters ->

        DiscreteChoiceModel(
            name = name,
            utilityAssignment = this.build(),
            distributionFunction = MultinomialLogit(),
            selectionFunction = RandomWeightedSelect(),
            parameters = parameters
        )
    }

fun <A, C, P> UtilityAssignmentBuilder<A, C, P>.multinomialLogit(
    name: String,
    choices: Set<A>,
): EnumeratedDiscreteModelBuilder<A, C, P> =
    EnumeratedDiscreteModelBuilder { parameters ->

        DiscreteChoiceModel(
            name = name,
            utilityAssignment = this.build(),
            distributionFunction = MultinomialLogit(),
            selectionFunction = RandomWeightedSelect(),
            parameters = parameters
        ).with(choices)
    }

fun <A, C, P> UtilityEnumerationBuilder<A, C, P>.multinomialLogit(
    name: String,
): EnumeratedDiscreteModelBuilder<A, C, P> =
    this.multinomialLogit(name, this.build().options)

fun <A, C, P, B> B.nestedLogit(
    name: String,
): EnumeratedDiscreteModelBuilder<A, C, P>
        where B : UtilityEnumerationBuilder<A, C, P>, B : NestedStructureDataBuilder<A, C, P> =
    EnumeratedDiscreteModelBuilder { parameters ->

        val utility = this.build()
        val structure = this.buildStructure()

        DiscreteChoiceModel<A, C, P>(
            name = name,
            utilityAssignment = utility,
            distributionFunction = NestedLogit(structure),
            selectionFunction = RandomWeightedSelect(),
            parameters = parameters
        ).with(utility.options)
    }

fun <A, C, P, B> B.crossNestedLogit(
    name: String,
): EnumeratedDiscreteModelBuilder<A, C, P>
        where
        B : UtilityEnumerationBuilder<A, C, P>,
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
