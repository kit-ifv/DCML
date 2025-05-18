package modeling.discreteChoice.utility

inline fun <K, V> Iterable<K>.associateWithNotNull(
    valueSelector: (K) -> V?
): Map<K, V> = this.mapNotNull { key -> valueSelector(key)?.let { key to it } }.toMap()