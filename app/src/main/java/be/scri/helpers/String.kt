package be.scri.helpers

operator fun String.times(x: Int): String {
    val stringBuilder = StringBuilder()
    for (i in 1..x) {
        stringBuilder.append(this)
    }
    return stringBuilder.toString()
}
