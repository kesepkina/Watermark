fun printIfPrime(number: Int) {
    var flag = "a prime number"
    for ( i in 2 until number) {
        if (number % i == 0) {
            flag = "not $flag"
            break
        }
    }
    println("$number is $flag.")
}

fun main(args: Array<String>) {
    val number = readln().toInt()
    printIfPrime(number)
}