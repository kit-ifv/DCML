package modeling.discreteChoice.utility

import kotlin.math.sign

inline fun <K, V> Iterable<K>.associateWithNotNull(
    valueSelector: (K) -> V?
): Map<K, V> = this.mapNotNull { key -> valueSelector(key)?.let { key to it } }.toMap()

fun String.indentSubsequentLines(indentMarker: String = ":", prefix: String = ""): String {
    val indentSize = (this.indexOf(indentMarker) + 2 - prefix.length).let {
        if (it < 0) 1 else it
    }

    val lines = this.lines()
    if (lines.size <= 1) {
        return this
    }

    return lines.let {
        lines.first() + "\n" +
                lines.drop(1).joinToString("\n") { line ->
                    prefix + " ".repeat(indentSize) + line
                }
    }
}

/**
 * Picks the key from the map which corresponds to the representative value of the random number.
 */
fun <K> Map<K, Double>.select(random: Double): K {
    require(isNotEmpty()) {
        "Cannot pick a value from an empty map"
    }
    require(random in 0.0..1.0) {
        "Need a random value between 0 and 1"
    }
    val target = normalize().cumulativeSum()
    val ins = target.binarySearch { it.first.compareTo(random) }
    val choice = target[ins.toIndex()].second
    return choice
}

fun <K> Map<K, Double>.normalize(): Map<K, Double> {
    val sum = values.sum()
    val copy = this.toMutableMap()
    copy.forEach {
        copy[it.key] = it.value / sum
    }
    return copy
}

fun <K> Map<K, Double>.cumulativeSum(): List<Pair<Double, K>> {
    val cumSum = values.cumulativeSum()
    return cumSum.zip(keys)
}

fun Collection<Number>.cumulativeSum(): List<Double> {
    val result = mutableListOf<Double>()
    var sum = 0.0

    for (number in this) {
        sum += number.toDouble()
        result.add(sum)
    }

    return result
}

/**
 * Converts the return value of [binarySearch] to the index position
 */
fun Int.toIndex(): Int {
    return if (sign == -1) {
        -this - 1
    } else {
        this
    }
}