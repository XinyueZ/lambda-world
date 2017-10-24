import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    takeIf()
    takeUnless()
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