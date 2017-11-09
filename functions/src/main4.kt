package functions.src

import logln

fun main(args: Array<String>) {
    lambdaInMap()
}

fun lambdaInMap() {
    val func1 = { logln("lambdaInMap") }
    val func2 = { logln("lambdaInMapMap") }
    val map = mapOf(func1 to "hello,world", func2 to "bye, world")
    logln(map[func1]!!)
    logln(map[func2]!!)
}