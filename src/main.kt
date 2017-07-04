fun main(args: Array<String>) {
    println("Examples ....")
    println()

    val list1 = listOf("one", "two", "three")
    foreachList(list1)
    val list2 = list1 + listOf("four", "five", "six")
    streamForeachList(list2)
    translateNumbers(list2)
    sum(list2)
    sumParallel(list2)
    sumAdvanced(list2)
}

fun foreachList(vals: List<String>) {
    println("[foreach] on list")
    vals.forEach({ println(it) })
}

fun streamForeachList(vals: List<String>) {
    println("[stream.foreach] on list")
    vals.stream().forEach({ println(it) })
}

fun translateNumbers(vals: List<String>) {
    println("[stream.mapToInt.foreach] on list")
    vals.stream().mapToInt {
        when (it) {
            "one" -> 1
            "two" -> 2
            "three" -> 3
            "four" -> 4
            "five" -> 5
            "six" -> 6
            else -> -1
        }
    }.forEach { println(it) }
}

fun sum(vals: List<String>) {
    println("[stream.mapToInt.sum] on list")
    vals.stream().filter({
        if (vals.indexOf(it) == vals.size - 1) print(it + " = ") else print(it + " + ")
        true
    }).mapToInt {
        when (it) {
            "one" -> 1
            "two" -> 2
            "three" -> 3
            "four" -> 4
            "five" -> 5
            "six" -> 6
            else -> -1
        }
    }.sum().let { println(it) }
}

fun sumParallel(vals: List<String>) {
    println("[stream.mapToInt.parallel.sum] on list")
    vals.stream().parallel().filter({
        if (vals.indexOf(it) == vals.size - 1) print(it + " = ") else print(it + " + ")
        true
    }).mapToInt {
        when (it) {
            "one" -> 1
            "two" -> 2
            "three" -> 3
            "four" -> 4
            "five" -> 5
            "six" -> 6
            else -> -1
        }
    }.sum().let { println(it) }
}

//fun sumAdvanced(vals: List<String>) {
//    println("[stream.mapToInt.toList] on list")
//
//    vals.stream().mapToInt { }
//
////     vals.stream().mapToInt { value: String? ->
////        when (value) {
////            "one" -> 1
////            "two" -> 2
////            "three" -> 3
////            "four" -> 4
////            "five" -> 5
////            "six" -> 6
////            else -> -1
////        }
////    }
//
////    nums.stream().filter({
////        if (nums.indexOf(it) == nums.size - 1) print(it.toString() + " = ") else print(it.toString() + " + ")
////        true
////    }).mapToInt { it }.sum().let { println(it.toString()) }
//}