//object Me {
//    const val CURRENT_AGE = 18
//    const val EYES_COLOR = "green"
//    const val HEIGHT = 188
//}

fun main() {
    val a = mutableListOf(1, 3, 5, 6)
    val b = mutableListOf(1, 3)
    a.removeAll(b)
    a.retainAll(b)
}