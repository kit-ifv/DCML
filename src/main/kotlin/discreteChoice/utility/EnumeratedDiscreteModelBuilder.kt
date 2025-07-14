package discreteChoice.utility

import discreteChoice.DiscreteChoiceModel
import discreteChoice.EnumeratedDiscreteChoiceModel
import discreteChoice.distribution.CrossNestedLogit
import discreteChoice.distribution.CrossNestedStructureDataBuilder
import discreteChoice.distribution.MultinomialLogit
import discreteChoice.distribution.NestedLogit
import discreteChoice.distribution.NestedStructureDataBuilder
import discreteChoice.models.ChoiceFilter
import discreteChoice.models.noFilter
import discreteChoice.selection.RandomWeightedSelect
import discreteChoice.structure.UtilityAssignmentBuilder
import discreteChoice.structure.UtilityEnumerationBuilder

fun interface DiscreteModelBuilder<R, A, P> {
    fun build(parameters: P): DiscreteChoiceModel<R, A, P>
}

fun interface EnumeratedDiscreteModelBuilder<R, A, P> {
    fun build(parameters: P): EnumeratedDiscreteChoiceModel<R, A, P>
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

fun <R, A, P, B> B.crossNestedLogit(
    name: String,
    filter: ChoiceFilter<A> = noFilter(),
): EnumeratedDiscreteModelBuilder<R, A, P>
        where
        B : UtilityEnumerationBuilder<R, A, P>,
        B : CrossNestedStructureDataBuilder<R, A, P> =
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
