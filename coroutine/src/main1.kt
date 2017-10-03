import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

const val Time1 = 2L
const val Time2 = 5L
val sumTime = Time1 + Time2

fun add(x: Int, y: Int) = x + y

fun main(args: Array<String>) {
    doSomethingAsync(false)
    doSomethingAsync(true)
}

fun doSomethingAsync(depend: Boolean) {
    when (depend) {
        false -> noDependency()
        true -> dependency()
    }
}

suspend fun getNum1(): Int {
    delay(Time1, TimeUnit.SECONDS)
    println("$Time1 sec call")
    return 50
}

suspend fun getNum2(): Int {
    delay(Time2, TimeUnit.SECONDS)
    println("$Time2 sec call")
    return 50
}

fun noDependency() {
    println("Call two functions which don't depend each other, pretend calling on the remote server, wait for some minutes less than $sumTime seconds........")

    runBlocking {
        val time = measureTimeMillis {
            val num1 = async(CommonPool) { getNum1() }
            val num2 = async(CommonPool) { getNum2() }
            val result = async(CommonPool) {
                println("The answer: ${add(num1.await(), num2.await())}")
            }
            result.await()
            println("Finish computing")
        }
        println("Completed in $time ms")
    }
}

fun dependency() {
    println("Call two functions which depend each other, need about $sumTime  seconds.......")

    runBlocking {
        val time = measureTimeMillis {
            val num1 = getNum1()
            val num2 = getNum2()
            val addedResult = add(num1, num2)
            println("The answer: $addedResult")

        }
        println("Completed in $time ms")
    }
}





