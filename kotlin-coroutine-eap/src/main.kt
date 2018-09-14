import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    //impatientWait()
    //patientWait()
    //patientWaitUntilOtherFinish()
    patientWaitUntilChildFinish()
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@d1be1c9d970e29fcc177bb3767087af48935d400/-/blob/coroutines-guide.md#bridging-blocking-and-non-blocking-worlds
fun impatientWait() = runBlocking {
    val bkTaskDuration = 5000L
    val impatientWait = 1000L
    GlobalScope.launch {
        delay(bkTaskDuration)
        println("impatientWait background coroutine with GlobalScope.launch.")
    }
    println("I am first, I want to wait: $impatientWait.")
    delay(impatientWait)
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@d1be1c9d970e29fcc177bb3767087af48935d400/-/blob/coroutines-guide.md#bridging-blocking-and-non-blocking-worlds
fun patientWait() = runBlocking {
    val bkTaskDuration = 5000L
    val patientWait = 5000L * 5
    GlobalScope.launch {
        delay(bkTaskDuration)
        println("impatientWait background coroutine with GlobalScope.launch.")
    }
    println("I am first, I want to wait: $patientWait.")
    delay(patientWait)
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@d1be1c9d970e29fcc177bb3767087af48935d400/-/blob/coroutines-guide.md#waiting-for-a-job
fun patientWaitUntilOtherFinish() = runBlocking  {
    val bkTaskDuration = 5000L
    val other = GlobalScope.launch {
        delay(bkTaskDuration)
        println("impatientWait background coroutine with GlobalScope.launch.")
    }
    println("I am first, I want to wait until other finishing.")
    //Main coroutine (runBlocking) is not tied to the duration of the background job in any way.
    other.join()
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@d1be1c9d970e29fcc177bb3767087af48935d400/-/blob/coroutines-guide.md#structured-concurrency
fun patientWaitUntilChildFinish() = runBlocking  {
    val bkTaskDuration = 5000L
    this.launch {//This is my child(runBlocking main)
        delay(bkTaskDuration)
        println("impatientWait background coroutine with GlobalScope.launch.")
    }
    println("I am first, I want to wait until my child being finishing.")
    //An outer coroutine (runBlocking in our example) does not complete until all the coroutines launched in its scope complete.
}