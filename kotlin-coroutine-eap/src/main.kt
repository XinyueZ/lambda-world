import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.0-eap13/-/blob/coroutines-guide.md#debugging-coroutines-and-threads
// Run without -Dkotlinx.coroutines.debug
//fun log(msg: String) = Thread.currentThread().run { println("[$name @coroutine#$id] $msg") }
// Run with -Dkotlinx.coroutines.debug
fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

fun main(args: Array<String>) {
    //impatientWait()
    //patientWait()
    //patientWaitUntilOtherFinish()
    //patientWaitUntilChildFinish()
    //patientWaitUntilChildInOtherScopeFinish()
    //patientWaitUntilChildInOtherScopeCancelled()
    //patientWaitUntilCancelHeavyJob()
    //patientWaitUntilTimeout()

    //sequential()
    //concurrent()
    //concurrent(true)
    //structuredConcurrency()
    //dispatchers()

    //longTimeChildWouldBeStopped()
    //parentalResponsibility()

    /**
     * Advance topic of coroutine, normally we don't use in app development.
     */
    advanceTopicThreadLocal()
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@d1be1c9d970e29fcc177bb3767087af48935d400/-/blob/coroutines-guide.md#bridging-blocking-and-non-blocking-worlds
fun impatientWait() = runBlocking {
    val bkTaskDuration = 5000L
    val impatientWait = 1000L
    GlobalScope.launch {
        delay(bkTaskDuration)
        log("messaging...")
    }
    log("I am first, I want to wait: $impatientWait.")
    delay(impatientWait)
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@d1be1c9d970e29fcc177bb3767087af48935d400/-/blob/coroutines-guide.md#bridging-blocking-and-non-blocking-worlds
fun patientWait() = runBlocking {
    val bkTaskDuration = 5000L
    val patientWait = 5000L * 5
    GlobalScope.launch {
        delay(bkTaskDuration)
        log("messaging...")
    }
    log("I am first, I want to wait: $patientWait.")
    delay(patientWait)
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@d1be1c9d970e29fcc177bb3767087af48935d400/-/blob/coroutines-guide.md#waiting-for-a-job
fun patientWaitUntilOtherFinish() = runBlocking {
    val bkTaskDuration = 5000L
    val other = GlobalScope.launch {
        delay(bkTaskDuration)
        log("messaging...")
    }
    log("I am first, I want to wait until other finishing.")
    //Main coroutine (runBlocking) is not tied to the duration of the background job in any way.
    other.join()
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@d1be1c9d970e29fcc177bb3767087af48935d400/-/blob/coroutines-guide.md#structured-concurrency
fun patientWaitUntilChildFinish() = runBlocking {
    val bkTaskDuration = 5000L
    this.launch {
        //This is my child(runBlocking main)
        delay(bkTaskDuration)
        log("messaging...")
    }
    log("I am first, I want to wait until my child being finishing.")
    //An outer coroutine (runBlocking in our example) does not complete until all the coroutines launched in its scope complete.
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.0-eap13/-/blob/coroutines-guide.md#scope-builder
fun patientWaitUntilChildInOtherScopeFinish() = runBlocking {
    val bkTaskDuration = 5000L
    val newScopeBkTaskDuration = 20000L
    launch {
        //This is my child(runBlocking main)
        delay(bkTaskDuration)
        log("messaging...")
    }

    coroutineScope {
        //An other long-time blocking which doesn't complete until child finishes.
        //A new scope which blocks main(runBlocking).
        this.launch {
            //A child of the new scope
            delay(newScopeBkTaskDuration)
            log("receiving...")
        }
        log("The child in the new scope.")
    }

    log("I am first, I want to wait until my child being finishing.")
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
        log("messaging...")
    }

    coroutineScope {
        //An other long-time blocking which doesn't complete until child finishes.
        //A new scope which blocks main(runBlocking).
        val child = this.launch(Dispatchers.Default) {
            //A child of the new scope
            delay(newScopeBkTaskDuration)
            log("receiving...")
        }
        log("The child in the new scope.")
        delay(cancelDuration)
        child.cancel()
        child.join()
        log("I have no patient, cancel this child.")
    }

    log("I am first, I want to wait until my child being finishing.")
    //An outer coroutine (runBlocking in our example) does not complete until all the coroutines launched in its scope complete.
    //The main difference between runBlocking and coroutineScope is that the latter does not block the current thread while waiting for all children to complete.
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.0-eap13/-/blob/coroutines-guide.md#making-computation-code-cancellable
fun patientWaitUntilCancelHeavyJob() = runBlocking {
    val bkTaskDuration = 1000L
    val cancelDuration = 1300L
    val longJobDuration = 5000L

    launch {
        //This is my child(runBlocking main)
        delay(bkTaskDuration)
        log("messaging...")
    }

    coroutineScope {
        //An other long-time blocking which doesn't complete until child finishes.
        //A new scope which blocks main(runBlocking).
        val child = this.launch(Dispatchers.Default) {
            //A child of the new scope
            var itor = 100000000000000

            try {
                while (itor >= 0 && isActive) { //Remove isActive, the job is so heavy and cannot be cancelled.
                    log("receiving...$itor")
                    itor--
                }
            } finally {
                withContext(NonCancellable) {
                    //Without this, the delay(5000)will block finally{}, and no log(), however, the finally will be cancelled.
                    delay(longJobDuration) //This is a suspend functions(blocking) which can do a bit long.
                    log("final receiving...$itor")
                }
            }
        }
        log("The child in the new scope.")
        delay(cancelDuration)
        child.cancel()
        log("Wait for stop.")
        child.join()
        log("I have no patient, cancel this child.")
    }

    log("I am first, I want to wait until my child being finishing.")
    //An outer coroutine (runBlocking in our example) does not complete until all the coroutines launched in its scope complete.
    //The main difference between runBlocking and coroutineScope is that the latter does not block the current thread while waiting for all children to complete.
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.0-eap13/-/blob/coroutines-guide.md#timeout
fun patientWaitUntilTimeout() = runBlocking {
    val bkTaskDuration = 20 * 1000L
    val timeout = 5000L
    val longJobDuration = 5000L

    launch {
        //This is my child(runBlocking main)
        delay(bkTaskDuration)
        log("messaging...")
    }

    withTimeoutOrNull(timeout) {
        //A child of the new scope
        var itor = 100000000000000

        try {
            while (itor >= 0 && isActive) { //Remove isActive, the job is so heavy and cannot be cancelled.
                log("receiving...$itor")
                itor--
            }
        } finally {
            withContext(NonCancellable) {
                //Without this, the delay(5000)will block finally{}, and no log(), however, the finally will be cancelled.
                delay(longJobDuration) //This is a suspend functions(blocking) which can do a bit long.
                log("final receiving...$itor")
            }
        }
    }

    log("I am first, I want to wait until my child being finishing.")
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.0-eap13/-/blob/coroutines-guide.md#sequential-by-default
fun sequential() = runBlocking {
    //The sequential is default in coroutine.
    //For concurrent try to use async{} explicitly, see example below: concurrent
    val one = doOne()
    //one blocks
    val two = doTwo()
    //two blocks

    //After blocking of one and two then code is operating on these.
    log("You're so lazy, one, two")
    log("result: ${one + two}")
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.0-eap13/-/blob/coroutines-guide.md#concurrent-using-async
/**
 * [lazy] is true, then starting each job explicitly.
 */
fun concurrent(lazy: Boolean = false) = runBlocking {
    if (lazy) {
        val one = async(start = LAZY) { doOne() }
        val two = async(start = LAZY) { doTwo() }

        //Explicitly start concurrent jobs.
        one.start() // start the first one
        two.start() // start the second one

        //one and two doesn't block each other.

        //Show this firstly, because one, two don't block.
        //runBlocking hasn't been blocked by one or two
        log("Command on one, two")
        //After running of one and two then code is operating on these.
        log("result: ${one.await() + two.await()}")

        /*
         * that if we have called await in log and omitted start on individual coroutines,
         * then we would have got the sequential behaviour as await starts the coroutine execution and
         * waits for the execution to finish, which is not the intended use-case for laziness.
         */
    } else {
        val one = async { doOne() }
        val two = async { doTwo() }
        //one and two doesn't block each other.

        //Show this firstly, because one, two don't block.
        //runBlocking hasn't been blocked by one or two
        log("Hi one, two")
        //After running of one and two then code is operating on these.
        log("result: ${one.await() + two.await()}")
    }
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.0-eap13/-/blob/coroutines-guide.md#structured-concurrency-with-async
fun structuredConcurrency() = runBlocking {
    launch(Dispatchers.IO) {
        //Don't want to block log below with launch{},
        //remove this if you want to see log later after trouble-maker runs.

        try {
            troubleMaker()
        } catch (e: Exception) {
            log("Trouble caused by: $e")
        }
    }
    log("Rescued from trouble-maker.")
}

private suspend fun troubleMaker() = coroutineScope {
    val y = async {
        try {
            delay(5000) // Something goes run between doOne and doTwo.
            val y = 1 / 0
            log("I got: $y")
        } finally {
            log("Something wrong with the expression: 1/0")
        }
    }
    val z = async {
        val z = doOne() + doTwo()
        log("one + two = $z") //Should not show, something goes run above.
    }
    awaitAll(y, z)
    /**
     * Run below only for results to operate, like x + y etc.
     */
    y.await()
    z.await()
}

private suspend fun doOne(): Int {
    log("do one")
    delay(3000)
    return 1
}

private suspend fun doTwo(): Int {
    log("do two")
    delay(3000)
    return 2
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.0-eap13/-/blob/coroutines-guide.md#dispatchers-and-threads
fun dispatchers() = runBlocking {
    launch {
        //Inherits the context (and thus dispatcher) from the CoroutineScope that it is being launched from.
        //In this case, it inherits the context of the main runBlocking coroutine which runs in the main thread.

        log("main runBlocking")
    }
    launch(Dispatchers.Unconfined) {
        //The Dispatchers.Unconfined coroutine dispatcher starts coroutine in the caller thread,
        //but only until the first suspension point. After suspension it resumes in the thread
        //that is fully determined by the suspending function that was invoked.
        //Unconfined dispatcher is appropriate when coroutine does not consume CPU time nor updates
        //any shared data (like UI) that is confined to a specific thread.

        log("Unconfined")
        delay(500)
        log("Unconfined")

        /*
         * the coroutine that had inherited context of runBlocking {...} continues to execute in the main thread,
         * while the unconfined one had resumed in the default executor thread that delay function is using.
         */
    }
    launch(Dispatchers.Default) {
        //The default dispatcher, that is used when coroutines are launched in GlobalScope,
        //is represented by Dispatchers.Default and uses shared background pool of threads,
        //so launch(Dispatchers.Default) { ... } uses the same dispatcher as GlobalScope.launch { ... }.

        log("Default")
    }
    launch(newSingleThreadContext("MyOwnThread")) {
        //Creates a new thread for the coroutine to run. A dedicated thread is a very expensive resource.
        //In a real application it must be either released, when no longer needed, using close function,
        //or stored in a top-level variable and reused throughout the application.

        log("newSingleThreadContext")
    }
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.0-eap13/-/blob/coroutines-guide.md#children-of-a-coroutine
fun longTimeChildWouldBeStopped() = runBlocking {
    val runner = launch {
        //Inherits the context (and thus dispatcher) from the CoroutineScope that it is being launched from.
        //In this case, it inherits the context of the main runBlocking coroutine which runs in the main thread.

        GlobalScope.launch {
            (0..1000000000).forEach {
                println("$it")
            }
        }

        launch(Dispatchers.IO) {
            //Creates a new thread for the coroutine to run. A dedicated thread is a very expensive resource.
            //In a real application it must be either released, when no longer needed, using close function,
            //or stored in a top-level variable and reused throughout the application.

            repeat(10000000000000000.toInt()) {
                if (isActive) {
                    println("Hi, Peter: $isActive")
                } else {
                    return@repeat
                }
            }
        }
    }

    println("Long time child.....")
    delay(2000)
    runner.cancelAndJoin()
    delay(5000)
    println("Wait some minutes to see what GlobalScope.launch is still doing.")
}

//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.0-eap13/-/blob/coroutines-guide.md#parental-responsibilities
fun parentalResponsibility() = runBlocking {
    val runner = launch {
        launch(Dispatchers.IO) {
            delay(10 * 1000)
            println("Internal: I'm done.")
        }
        delay(5 * 1000)

        println("I must wait Internal done.")
    }

    println("I'm waiting for all children.")
    runner.join()//Explicitly wait until runner and its children being done.
    println("All done")
}

val threadLocal = ThreadLocal<String?>()
//https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.0-eap13/-/blob/coroutines-guide.md#thread-local-data
fun advanceTopicThreadLocal() = runBlocking {
    //This sample explains how a coroutine runs based on different threads or thread from a thread-pool.
    //There's a shared value which can be accessed between threads.

    threadLocal.set("main")
    log("Pre-main, thread local value: '${threadLocal.get()}'")
    val job = launch(Dispatchers.Default + threadLocal.asContextElement(value = "launch")) {
        log("Launch start, thread local value: '${threadLocal.get()}'")
        yield() //Give one other thread from dispatcher of this launch{}.
        log("After yield, thread local value: '${threadLocal.get()}'")
    }
    job.join()
    log("Post-main, thread local value: '${threadLocal.get()}'")
}