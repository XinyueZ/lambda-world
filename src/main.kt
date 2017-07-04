import java.util.stream.IntStream
import java.util.stream.Stream

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

fun sumAdvanced(vals: List<String>) {
    println("[stream + stream] on list")
    vals.stream().flatMapToInt {
        Stream.of(when (it) {
            "one" -> Pair(1, 1)//index or position, value
            "two" -> Pair(2, 2)
            "three" -> Pair(3, 3)
            "four" -> Pair(4, 4)
            "five" -> Pair(5, 5)
            "six" -> Pair(6, 7)
            else -> null
        }).filter {
            if (it!!.first == vals.size) print(it.first.toString() + " = ") else print(it.first.toString() + " + ")
            true
        }.flatMapToInt { IntStream.of(it!!.second) }
    }.sum().let { println(it) }

}