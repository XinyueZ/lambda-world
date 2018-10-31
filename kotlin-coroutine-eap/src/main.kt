
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
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
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield

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

//    exceptionAutomatically()
//    exceptionExposed()
//    exceptionHandler()
    structuredConcurrencyWithExceptionHandler()

    /**
     * Advance topic of coroutine, normally we don't use in app development.
     */
    //advanceTopicThreadLocal()
}

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
                    val factor = itor * 0.1
                    log("receiving...$factor")
                    itor--
                }
            } finally {
                log("I am killed.")
                withContext(NonCancellable) {
                    //Attempt the following long-term codes to be proceed.
                    //Without this, the delay(5000) will throw a CancellationException,
                    //but the no problem, because the parent job has been canceled
                    delay(longJobDuration)
                    log("final receiving...${itor * 0.1}")
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

fun patientWaitUntilTimeout() = runBlocking {
    val bkTaskDuration = 20 * 1000L
    val timeout = 5000L
    val longJobDuration = 5000L

    launch {
        //This is my child(runBlocking main)
        delay(bkTaskDuration)
        log("messaging...")
    }

    try {
        //withTimeoutOrNull(timeout) {
        withTimeout(timeout) {
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
    } catch (e: TimeoutCancellationException) {
        log("Time is up due to withTimeout(), not with withTimeoutOrNull().")
    }

    log("I am first, I want to wait until my child being finishing.")
}

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

fun structuredConcurrency() = runBlocking {
    launch(Dispatchers.IO) {
        //Don't want to block log below with launch{},
        //remove this if you want to see log later after trouble-maker runs.

        try {
            //The coroutine 'z' troubleMaker() can't be fired as expected, because there's in
            //the coroutine 'y' which breaks whole coroutineScope{}.

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
            delay(5000) // Something goes run during running of doOne and doTwo.
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

@ObsoleteCoroutinesApi //for newSingleThreadContext
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
        //Unconfined dispatcher ipatientWaitUntilCancelHeavyJobs appropriate when coroutine does not consume CPU time nor updates
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

fun exceptionAutomatically() = runBlocking {
    //The receiver-like coroutine: launch or actor can consume exception internal automatically.

    val request = GlobalScope.launch {
        delay(5000)
        val y = 1 / 0
        log("result: $y")
    }
    log("Sent request")
    request.join()//Waiting for the result.
    log("Done")//See this, the program will be processed here.
}

fun exceptionExposed() = runBlocking {
    //The sender-like coroutine: async or produce exposes exception to the coroutine's caller.

    val request = GlobalScope.async {
        delay(5000)
        val y = 1 / 0
        y
    }
    log("Sent request")

    try {
        awaitAll(request)
        log("result: ${request.await()}")//See this, the program won't be processed here.
    } catch (e: Exception) {
        log("Something is wrong caused by: $e")//See this, the program will be processed here.
    }
}

fun exceptionHandler() = runBlocking {
    //Exception handler, to customize exception catching.
    //The handler works only for exceptionAutomatically not exceptionExposed.

    val handler = CoroutineExceptionHandler { _, e ->
        log("Something is wrong caused by: $e")//See this, the program will be processed here.
    }

    val request1 = GlobalScope.launch(handler) {
        delay(5000)
        val y = 1 / 0
        log("result: $y")
    }
    log("Sent request1")
    request1.join()//Waiting for the result.
    log("Done~1")//See this, the program will be processed here.

    val request2 = GlobalScope.async(handler) {
        delay(5000)
        val y = 1 / 0
        y
    }
    log("Sent request2")

    log("result: ${request2.await()}")//The program won't be processed here.
    log("Done~2")//See this, the program won't be processed here.
}

fun structuredConcurrencyWithExceptionHandler() = runBlocking {
    //Different from structuredConcurrency(), here an exception-handler is used.
    //GlobalScope.launch is used to be parent of to troubleMakerHelper() which sends
    //some errors.

    //This also a reason why, in these examples, CoroutineExceptionHandler is always installed to a
    //coroutine that is created in GlobalScope. It does not make sense to install an exception
    //handler to a coroutine that is launched in the scope of the main runBlocking,
    //since the main coroutine is going to be always cancelled when its child completes with
    //exception despite the installed handler.

    //For non-GlobalScope coroutines: Child killed, parent killed, parent of parent killed, siblings killed.
    //The exception will be exposed from internal to outside scopes to handle.
    //When same level coroutine has try-catch on an exception, the catch must cancel other coroutines in the same level.
    //See: troubleMakerHelper2()

    val handler = CoroutineExceptionHandler { _, e ->
        log("Something is wrong caused by: $e")//See this, the program will be processed here.
    }

    //With GlobalScope, exception will be handled.
    GlobalScope.launch(handler) {
        troubleMakerHelper1()
    }.join()
    log("End~1")

    //If a coroutine encounters exception other than CancellationException,
    //it cancels its parent with that exception.
    //This behaviour cannot be overridden and is used to provide stable coroutines
    //hierarchies for structured concurrency which do not depend on CoroutineExceptionHandler implementation.

    //The original exception is handled by the parent when all its children terminate.
    try {
        troubleMakerHelper1()
    } catch (e: Exception) {
        log("I can save troubleMakerHelper1 caused by $e")
    }

    //This variant handles exception internally.
    troubleMakerHelper2()

    log("End~2")
}

private suspend fun troubleMakerHelper1() = coroutineScope {
    val z = async {
        var z = 0
        try {
            delay(51000)  //The 'y' makes error, which cancels parent, and all children will be also canceled.
            log("troubleMakerHelper 1 done ")
        } finally {
            withContext(NonCancellable) {
                z = doOne() + doTwo()
                log("one + two = $z") //Show, 'z' is a child, it is canceled.
            }
        }
        z
    }

    val y = async {
        try {
            delay(1000) // Something goes run during running of 'z'.
            val y = 1 / 0
            log("I got: $y")
        } finally {
            log("Something wrong with the expression: 1/0")
        }
    }
    //Expose exception, it breaks all,
    //cancel siblings, parent, bubble exception to parent.
    awaitAll(z, y)
}

private suspend fun troubleMakerHelper2() = coroutineScope {
    val z = async {
        var z = 0
        try {
            delay(51000)  //The 'y' makes error, which cancels parent, and all children will be also canceled.
            log("troubleMakerHelper 2 done ")
        } finally {
            withContext(NonCancellable) {
                z = doOne() + doTwo()
                log("one + two = $z") //Show, 'z' is a child, it is canceled.
            }
        }
        z
    }

    val y = async {
        try {
            delay(1000) // Something goes run during running of 'z'.
            val y = 1 / 0
            log("I got: $y")
        } finally {
            log("Something wrong with the expression: 1/0")
        }
    }

    //Handle exception here, but coroutines will be canceled.
    //'z' is canceled manually, 'y' is automatically.
    try {
        awaitAll(z, y)
    } catch (e: Exception) {
        log("Handle exception internally, caused by $e")
        z.cancel()
    }
}