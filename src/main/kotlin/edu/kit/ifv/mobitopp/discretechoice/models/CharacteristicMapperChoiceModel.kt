package edu.kit.ifv.mobitopp.discretechoice.models

import kotlin.random.Random


/**
 * This is a FixedChoiceModel that delegates all calls to the delegateChoiceModel. It uses the [characteristicConverter]
 * to map the given characteristic to the required characteristic of the delegate.
 * This class was created to enable enriching the characteristic with more or arbitrary information.
 */
abstract class CharacteristicMapperChoiceModel<A, C1, C2>(
): FixedChoiceModel<A, C1>{
    abstract val delegateChoiceModel: FixedChoiceModel<A, C2>
    override val choices: Set<A> by lazy {
        delegateChoiceModel.choices
    }
    override val name: String by lazy {
        "Wrapped-${delegateChoiceModel.name}"
    }
    context(characteristic: C1)
    abstract fun characteristicConverter(): C2

    context(_: C1, random: Random)
    override fun select(): A {
        val delegateCharacteristic = characteristicConverter()
        context(delegateCharacteristic) {
            return delegateChoiceModel.select()
        }
    }

    context(_: C1, _: Random)
    override fun select(choices: Set<A>): A {
        val delegateCharacteristic = characteristicConverter()
        context(delegateCharacteristic) {
            return delegateChoiceModel.select(choices = choices)
        }
    }

    context(_: C1)
    override fun utility(alternative: A): Double {
        val delegateCharacteristic = characteristicConverter()
        context(delegateCharacteristic) {
            return delegateChoiceModel.utility(alternative = alternative)
        }
    }

    override fun probabilities(utilities: Map<A, Double>): Map<A, Double> {
        return delegateChoiceModel.probabilities(utilities = utilities)
    }

    context(_: C1, random: Random)
    override fun selectInjected(
        choices: Set<A>,
        injections: Map<A, (Double) -> Double>
    ): A {
        val delegateCharacteristic = characteristicConverter()
        context(delegateCharacteristic) {
            return delegateChoiceModel.selectInjected(choices = choices, injections = injections)
        }
    }
}