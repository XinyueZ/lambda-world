fun main(args: Array<String>) {
    println("Examples ....")
    println()

    val list: ArrayList<String> = ArrayList()
    list.add("one")
    list.add("two")
    list.add("three")
    foreachList(list)

    list.add("four")
    list.add("five")
    list.add("six")
    streamForeachList(list)
}

fun foreachList(vals: List<String>) {
    println("[foreach] on list")
    vals.forEach({ s -> println(s) })
}

fun streamForeachList(vals: List<String>) {
    println("[stream.foreach] on list")
    vals.stream().forEach({ s -> println(s) })
}