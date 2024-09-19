package be.scri.helpers

operator fun String.times(x: Int): String {
    val stringBuilder = StringBuilder()
    (1..x).forEach { stringBuilder.append(this) }
    return stringBuilder.toString()
}
