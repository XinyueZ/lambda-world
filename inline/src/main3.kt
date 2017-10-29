import java.util.concurrent.TimeUnit

class SomeHandler(var name: String = "") {
    override fun toString() = name
}

fun main(args: Array<String>) {
    takeIf()
    takeUnless()
    also()
    logln(run1())
    logln(run2().toString())
}

fun takeIf() {
    logln("Demo how to use [takeIf], the if-then")

    val listRandom = List(100) {
        TimeUnit.MILLISECONDS.sleep(60)
        System.currentTimeMillis()
    }

    listRandom.forEach {
        it.takeIf {
            it % 2 == 0L
        }?.let {
            logln("Even: $it")
        } ?: run {
            logln("Odd: $it")
        }
    }
}

fun takeUnless() {
    logln("Demo how to use [takeUnless], the if-not-then, the result is contrary to [takeIf]")

    val listRandom = List(100) {
        TimeUnit.MILLISECONDS.sleep(60)
        System.currentTimeMillis()
    }

    listRandom.forEach {
        it.takeUnless {
            it % 2 == 0L
        }?.let {
            logln("Odd: $it")
        } ?: run {
            logln("Even: $it")
        }
    }
}

fun also() {
    logln("Demo how to use [also]")
    SomeHandler().also { it.name = "bla bla bla using ALSO." }.let { logln(it.toString()) }
}

fun run1(): String {
    logln("Demo how to use [T.run]")
    return SomeHandler().also { it.name = "bla bla bla using ALSO." }.run {
        name = "I have done some changing."
        toString()
    }
}

fun run2(): Int {
    logln("Demo how to use [run]")
    return run {
        1234234
    }
}