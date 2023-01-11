import java.lang.NumberFormatException

fun intDivision(x: String, y: String): Int {
    var result = 0
    try {
        result = x.toInt() / y.toInt()
    } catch (e: NumberFormatException) {
        println("Read values are not integers.")
        return 0
    } catch (e: ArithmeticException) {
        println("Exception: division by zero!")
        return 0
    }
    return result
}

fun main() {
    val x = readLine()!!
    val y = readLine()!!
    println(intDivision(x, y))

}