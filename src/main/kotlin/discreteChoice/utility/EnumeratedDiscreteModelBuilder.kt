package discreteChoice.utility

import discreteChoice.DiscreteChoiceModel
import discreteChoice.EnumeratedDiscreteChoiceModel
import discreteChoice.distribution.CrossNestedLogit
import discreteChoice.distribution.CrossNestedStructureDataBuilder
import discreteChoice.distribution.MultinomialLogit
import discreteChoice.distribution.NestedLogit
import discreteChoice.distribution.NestedStructureDataBuilder
import discreteChoice.selection.RandomWeightedSelect
import discreteChoice.structure.UtilityAssignmentBuilder
import discreteChoice.structure.UtilityEnumerationBuilder
import discreteChoice.models.ChoiceAlternative
import discreteChoice.models.ChoiceFilter
import discreteChoice.models.noFilter

fun interface DiscreteModelBuilder<R : Any, A, P> where A : ChoiceAlternative<R> {
    fun build(parameters: P): DiscreteChoiceModel<R, A, P>
}

fun interface EnumeratedDiscreteModelBuilder<R : Any, A, P> where A : ChoiceAlternative<R> {
    fun build(parameters: P): EnumeratedDiscreteChoiceModel<R, A, P>
}

fun <R : Any, A, P> UtilityAssignmentBuilder<R, A, P>.openMultinomialLogit(
    name: String,
    filter: ChoiceFilter<A> = noFilter(),
): DiscreteModelBuilder<R, A, P> where A : ChoiceAlternative<R> =
    DiscreteModelBuilder { parameters ->

        DiscreteChoiceModel<R, A, P>(
            name = name,
            choiceFilter = filter,
            utilityAssignment = this.build(),
            distributionFunction = MultinomialLogit<A, P>(),
            selectionFunction = RandomWeightedSelect(),
            parameters = parameters
        )
    }

fun <R : Any, A, P> UtilityAssignmentBuilder<R, A, P>.multinomialLogit(
    name: String,
    choices: Set<R>,
    filter: ChoiceFilter<A> = noFilter(),
): EnumeratedDiscreteModelBuilder<R, A, P> where A : ChoiceAlternative<R> =
    EnumeratedDiscreteModelBuilder { parameters ->

        DiscreteChoiceModel<R, A, P>(
            name = name,
            choiceFilter = filter,
            utilityAssignment = this.build(),
            distributionFunction = MultinomialLogit<A, P>(),
            selectionFunction = RandomWeightedSelect(),
            parameters = parameters
        ).with(choices)
    }

fun <R : Any, A, P> UtilityEnumerationBuilder<R, A, P>.multinomialLogit(
    name: String,
    filter: ChoiceFilter<A> = noFilter()
): EnumeratedDiscreteModelBuilder<R, A, P> where A : ChoiceAlternative<R> =
    this.multinomialLogit(name, this.build().options, filter)

fun <R : Any, A, P, B> B.nestedLogit(
    name: String,
    filter: ChoiceFilter<A> = noFilter(),
): EnumeratedDiscreteModelBuilder<R, A, P>
    where A : ChoiceAlternative<R>, B : UtilityEnumerationBuilder<R, A, P>, B : NestedStructureDataBuilder<R, A, P> =
    EnumeratedDiscreteModelBuilder { parameters ->

        val utility = this.build()
        val structure = this.buildStructure()

        DiscreteChoiceModel<R, A, P>(
            name = name,
            choiceFilter = filter,
            utilityAssignment = utility,
            distributionFunction = NestedLogit(structure),
            selectionFunction = RandomWeightedSelect(),
            parameters = parameters
        ).with(utility.options)
    }

fun <R : Any, A, P, B> B.crossNestedLogit(
    name: String,
    filter: ChoiceFilter<A> = noFilter(),
): EnumeratedDiscreteModelBuilder<R, A, P>
    where A : ChoiceAlternative<R>,
          B : UtilityEnumerationBuilder<R, A, P>,
          B : CrossNestedStructureDataBuilder<R, A, P> =
    EnumeratedDiscreteModelBuilder { parameters ->

        val utility = this.build()
        val structure = this.buildStructure()

        DiscreteChoiceModel<R, A, P>(
            name = name,
            choiceFilter = filter,
            utilityAssignment = utility,
            distributionFunction = CrossNestedLogit(structure),
            selectionFunction = RandomWeightedSelect(),
            parameters = parameters
        ).with(utility.options)
    }
