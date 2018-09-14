import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    //impatientWait()
    //patientWait()
    //patientWaitUntilOtherFinish()
    //patientWaitUntilChildFinish()
    //patientWaitUntilChildInOtherScopeFinish()
    patientWaitUntilChildInOtherScopeCancelled()
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@d1be1c9d970e29fcc177bb3767087af48935d400/-/blob/coroutines-guide.md#bridging-blocking-and-non-blocking-worlds
fun impatientWait() = runBlocking {
    val bkTaskDuration = 5000L
    val impatientWait = 1000L
    GlobalScope.launch {
        delay(bkTaskDuration)
        println("messaging...")
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
        println("messaging...")
    }
    println("I am first, I want to wait: $patientWait.")
    delay(patientWait)
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@d1be1c9d970e29fcc177bb3767087af48935d400/-/blob/coroutines-guide.md#waiting-for-a-job
fun patientWaitUntilOtherFinish() = runBlocking {
    val bkTaskDuration = 5000L
    val other = GlobalScope.launch {
        delay(bkTaskDuration)
        println("messaging...")
    }
    println("I am first, I want to wait until other finishing.")
    //Main coroutine (runBlocking) is not tied to the duration of the background job in any way.
    other.join()
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@d1be1c9d970e29fcc177bb3767087af48935d400/-/blob/coroutines-guide.md#structured-concurrency
fun patientWaitUntilChildFinish() = runBlocking {
    val bkTaskDuration = 5000L
    this.launch {
        //This is my child(runBlocking main)
        delay(bkTaskDuration)
        println("messaging...")
    }
    println("I am first, I want to wait until my child being finishing.")
    //An outer coroutine (runBlocking in our example) does not complete until all the coroutines launched in its scope complete.
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.0-eap13/-/blob/coroutines-guide.md#scope-builder
fun patientWaitUntilChildInOtherScopeFinish() = runBlocking {
    val bkTaskDuration = 5000L
    val newScopeBkTaskDuration = 20000L
    launch {
        //This is my child(runBlocking main)
        delay(bkTaskDuration)
        println("messaging...")
    }

    coroutineScope {//An other long-time blocking which doesn't complete until child finishes.
        //A new scope which blocks main(runBlocking).
        this.launch {
            //A child of the new scope
            delay(newScopeBkTaskDuration)
            println("receiving...")
        }
        println("The child in the new scope.")
    }

    println("I am first, I want to wait until my child being finishing.")
    //An outer coroutine (runBlocking in our example) does not complete until all the coroutines launched in its scope complete.
    //The main difference between runBlocking and coroutineScope is that the latter does not block the current thread while waiting for all children to complete.
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.0-eap13/-/blob/coroutines-guide.md#cancelling-coroutine-execution
fun patientWaitUntilChildInOtherScopeCancelled() = runBlocking {
    val bkTaskDuration = 1000L
    val newScopeBkTaskDuration = 20000L
    val cancelDuration = 3000L

    launch {
        //This is my child(runBlocking main)
        delay(bkTaskDuration)
        println("messaging...")
    }

    coroutineScope { //An other long-time blocking which doesn't complete until child finishes.
        //A new scope which blocks main(runBlocking).
        val child = this.launch {
            //A child of the new scope
            delay(newScopeBkTaskDuration)
            println("receiving...")
        }
        println("The child in the new scope.")
        delay(cancelDuration)
        child.cancel()
        child.join()
        println("I have no patient, cancel this child.")
    }

    println("I am first, I want to wait until my child being finishing.")
    //An outer coroutine (runBlocking in our example) does not complete until all the coroutines launched in its scope complete.
    //The main difference between runBlocking and coroutineScope is that the latter does not block the current thread while waiting for all children to complete.
}