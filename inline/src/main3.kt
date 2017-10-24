import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    takeIf()
}

fun takeIf() {
    logln("Demo how to use [takeIf]")

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