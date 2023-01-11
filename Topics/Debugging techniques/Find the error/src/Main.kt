//import java.util.Scanner
//
//fun swapInts(ints: IntArray): IntArray {
//    return intArrayOf(ints[1], ints[0])
//}
//
//fun main() {
//    val scanner = Scanner(System.`in`)
//    while (scanner.hasNextLine()) {
//        var ints = intArrayOf(
//            scanner.nextLine().toInt(),
//            scanner.nextLine().toInt(),
//        )
//        ints = swapInts(ints)
//        println(ints[0])
//        println(ints[1])
//    }
//}

fun main() {
    var prev = 0
    var current = 1
    for (i in 0..24) {
        val swap = prev
        prev = current
        current += swap
        println(current)
    }
}