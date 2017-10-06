@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.experimental.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

fun log(msg: String) = print("[${Thread.currentThread().name}: ] $msg")
fun logln(msg: String) = println("[${Thread.currentThread().name}: ] $msg")

const val Time1 = 5L
const val Time2 = 5L
val sumTime = Time1 + Time2

fun add(x: Int, y: Int) = x + y

fun main(args: Array<String>) = runBlocking<Unit> {
    println("Main job is ${coroutineContext[Job]}")
    //join:  wait until launch child routine completes
    //aWait: wait until async child routine completes
    measureTimeMillis { doSomethingAsync(false).apply { join() } }.apply { logln("Completed noDependency in $this ms") }
    measureTimeMillis { doSomethingAsync(true).apply { join() } }.apply { logln("Completed dependency in $this ms") }
    measureTimeMillis { networkCall().apply { join() } }.apply { logln("Completed networkCall in $this ms") }
    measureTimeMillis { networkCallAsync().apply { join() } }.apply { logln("Completed networkCallAsync in $this ms") }
    measureTimeMillis {
        repeatFunction().apply {
            logln("main: I'm tired of waiting!")
            delay(1300) // Without this the application goes end, because coroutine only suspend not blocking.
            cancelAndJoin() // Must do here, otherwise output of this repeatFunction going on.
            logln("done, the repeat")
        }
    }.apply {
        logln("Completed repeatFunction in $this ms")
    }
    measureTimeMillis { repeatUnderTimer() } // Not do join() so that output shows behind next codes.
    measureTimeMillis {combineContext().apply { join() }}.apply { logln("Completed combineContext in $this ms")  }

    launch {
        println()
        logln("Last 30 sec wait for all jobs if possible")
        delay(30, TimeUnit.SECONDS)
    }.apply { join() }
}

fun doSomethingAsync(depend: Boolean) = when (depend) {
    false -> noDependency()
    true -> dependency()
}

fun noDependency() = launch(newSingleThreadContext("noDependency thread")) {
    logln("Call two functions which don't depend each other, pretend calling on the remote server, wait for some minutes less than $sumTime seconds........")

    val num1 = asyncGetNum1()
    val num2 = asyncGetNum2()
    logln("The answer: ${add(num1.await(), num2.await())}")
    logln("Finish computing")
}

fun dependency() = launch(Unconfined) {
    logln("Call two functions which depend each other, need about $sumTime  seconds.......")

    val num1 = getNum1()
    val num2 = getNum2()
    val addedResult = add(num1, num2)
    logln("The answer: $addedResult")
}

internal data class Hello(@field:SerializedName("id") val id: Int, @field:SerializedName("content") val content: String)
internal interface Service {
    @GET("greeting")
    fun greeting(): Call<Hello>


}

fun networkCall() = launch(CommonPool) {
    logln("Call some feeds normally, it needs some sec......")

    val response = getResponse()
    response.takeIf { it.isSuccessful }?.let {
        logln("response networkCall: ${it.body()}")
    } ?: kotlin.run {
        logln("Something wrong at getting response")
    }

    // Because JVM might end before response coming. We could miss the output.
    // If there're other functions behind this function, we would see output.
    // Otherwise you can delay for some minutes
    // with delay(5, TimeUnit.SECONDS).
    // Better:
    // Or: join() to wait until the child routine completes.
}

fun networkCallAsync() = launch(CommonPool) {
    logln("Call some feeds with async, it needs some sec......")

    async(CommonPool) {
        val response = getResponse()
        response.takeIf { it.isSuccessful }?.let {
            logln("response networkCallAsync: ${it.body()}")
        } ?: kotlin.run {
            logln("Something wrong at getting response")
        }
    }.apply { await() }
}

fun repeatFunction() = launch(CommonPool) {
    logln("Do something repeatably......")

    try {
        logln("Try to play with kotlin builtIn repeat()")
        val startTime = System.currentTimeMillis()
        var nextPrintTime = startTime
        var i = 0
        while (isActive) { // computation loop, just wastes CPU
            // print a message twice a second
            if (System.currentTimeMillis() >= nextPrintTime) {
                logln("I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    } finally {
        logln("finally in repeat")
    }
}

fun repeatUnderTimer() = launch(CommonPool) {
    withTimeoutOrNull(10, TimeUnit.SECONDS) {
        logln("Doing under 10 sec control, this job doesn't have join() later and will show behind ending message.")

        repeat(Int.MAX_VALUE) {
            print("[$it]:")

            for (i in 0..10)
                print(" $i ")

            delay(1, TimeUnit.SECONDS)
            println()
        }
    }.takeIf { it == null }.apply {
        println()
        logln("10 sec job done!")
    }
}

fun combineContext() = launch(newSingleThreadContext("parent")) {
    logln("Evaluation the combination of contexts")
    logln("echo parent")
    launch(
            newSingleThreadContext("child 1") + coroutineContext
    ) {
        logln("echo child 1")
        launch(
                newSingleThreadContext("child 2") + coroutineContext
        ) {
            logln("echo child 2")
            launch(
                    newSingleThreadContext("child 3")
            ) {
                logln("echo child 3")
            }
        }
    }
}

private fun getNum1() = 50.apply {
    TimeUnit.SECONDS.sleep(Time1)
    logln("$Time1 sec call")
}

private fun getNum2() = 50.apply {
    TimeUnit.SECONDS.sleep(Time2)
    logln("$Time2 sec call")
}

private fun asyncGetNum1() = async(CommonPool) {
    50.apply {
        delay(Time1, TimeUnit.SECONDS)
        logln("$Time1 sec call")
    }
}

private fun asyncGetNum2() = async(CommonPool) {
    50.apply {
        delay(Time2, TimeUnit.SECONDS)
        logln("$Time2 sec call")
    }
}

private suspend fun getResponse() = Retrofit.Builder().baseUrl("http://rest-service.guides.spring.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build().create(Service::class.java).greeting().execute()
