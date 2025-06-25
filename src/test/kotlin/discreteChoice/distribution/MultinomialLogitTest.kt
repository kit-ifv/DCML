package discreteChoice.distribution


import org.junit.jupiter.api.Test
import kotlin.assert

class MultinomialLogitTest {

    /**
     * When the utilities are big, the exponential function is quiet big. Should still work with utilities < 710.
     */
    @Test
    fun equalnearInfinity() {
        val logit = MultinomialLogit<Int, List<Int>>();
        val utilities: Map<Int, Double> = mapOf(1 to 1.0, 2 to 709.0, 3 to 709.0)
        val probabilities = logit.calculateProbabilities(utilities)

        println(probabilities)
        assert(0.001 > probabilities[1]!!)
        assert(0.499 < probabilities[2]!!)
        assert(0.499 < probabilities[3]!!)
    }

    /**
     * Testing edge case of utilities `>=710` because `exp(710) == Double.POSITIVE_INFINITY`.
     */
    @Test
    fun infinityTest() {
        val logit = MultinomialLogit<Int, List<Int>>();
        val utilities: Map<Int, Double> = mapOf(1 to 709.0, 2 to 710.0, 3 to 710.0)
        val probabilities = logit.calculateProbabilities(utilities)

        println(probabilities)
        assert(0.001 > probabilities[1]!!)
        assert(0.499 < probabilities[2]!!)
        assert(0.499 < probabilities[3]!!)
    }

    /**
     * If all utilities are very small <= -745. exp(-745) == 0.0 possibly leading to division through zero.
     * Testing that side effect. Expecting each option to get the same probability.
     */
    @Test
    fun zeroDivision() {
        val logit = MultinomialLogit<Int, List<Int>>();
        val utilities: Map<Int, Double> = mapOf(1 to -746.0, 2 to -7046.0, 3 to -746.0)
        val probabilities = logit.calculateProbabilities(utilities)

        println(probabilities)
        assert(0.33 < probabilities[1]!!)
        assert(0.33 < probabilities[2]!!)
        assert(0.33 < probabilities[3]!!)
    }

    /**
     * Testing utilities leading to near zero, but not division through zero.
     */
    @Test
    fun almostZeroDivision() {
        val logit = MultinomialLogit<Int, List<Int>>();
        val utilities: Map<Int, Double> = mapOf(1 to 0.0, 2 to -744.0, 3 to -744.0)
        val probabilities = logit.calculateProbabilities(utilities)

        println(probabilities)
        assert(0.999 < probabilities[1]!!)
        assert(0.001 > probabilities[2]!!)
        assert(0.001 > probabilities[3]!!)
    }
}