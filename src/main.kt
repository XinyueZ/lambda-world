fun main(args: Array<String>) {
    println("Examples ....")
    println()

    val list: ArrayList<String> = ArrayList()
    list.add("one")
    list.add("two")
    list.add("three")
    foreachList(
            list
    )
}

fun foreachList(vals: List<String>) {
    println("[foreach] on list")
    vals.forEach({ s -> println(s) })
}