fun main(args: Array<String>) {
    println("Examples ....")
    println()

    val list1= listOf("one", "two", "three")
    foreachList(list1)
    val list2 =  list1 + listOf("four", "five", "six")
    streamForeachList(list2)
    translateNumbers(list2)
}

fun foreachList(vals: List<String>) {
    println("[foreach] on list")
    vals.forEach({ s -> println(s) })
}

fun streamForeachList(vals: List<String>) {
    println("[stream.foreach] on list")
    vals.stream().forEach({ s -> println(s) })
}

fun translateNumbers(vals: List<String>) {
    println("[stream.mapToInt.foreach] on list")
    vals.stream().mapToInt { value: String? ->
        when (value) {
            "one" -> 1
            "two" -> 2
            "three" -> 3
            "four" -> 4
            "five" -> 5
            "six" -> 6
            else -> -1
        }
    }.forEach { s -> println(s) }
}