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

const val Time1 = 5L
const val Time2 = 5L
val sumTime = Time1 + Time2

fun add(x: Int, y: Int) = x + y

fun main(args: Array<String>) = runBlocking<Unit> {
    //join:  wait until launch child routine completes
    //aWait: wait until async child routine completes
    measureTimeMillis { doSomethingAsync(false).apply { join() } }.apply { println("Completed noDependency in $this ms") }
    measureTimeMillis { doSomethingAsync(true).apply { join() } }.apply { println("Completed dependency in $this ms") }
    measureTimeMillis { networkCall().apply { join() } }.apply { println("Completed networkCall in $this ms") }
    measureTimeMillis { networkCallAsync().apply { join() } }.apply { println("Completed networkCallAsync in $this ms") }
    measureTimeMillis { repeatFunction().apply { delay(5, TimeUnit.SECONDS) /*Without this the application goes end, because coroutine only suspend not blocking*/ } }.apply { println("Completed repeatFunction in $this ms") }
}

fun doSomethingAsync(depend: Boolean) = when (depend) {
    false -> noDependency()
    true -> dependency()
}

fun noDependency() = launch {
    println("Call two functions which don't depend each other, pretend calling on the remote server, wait for some minutes less than $sumTime seconds........")

    val num1 = async(CommonPool) { getNum1() }
    val num2 = async(CommonPool) { getNum2() }
    println("The answer: ${add(num1.await(), num2.await())}")
    println("Finish computing")
}

fun dependency() = launch {
    println("Call two functions which depend each other, need about $sumTime  seconds.......")

    val num1 = getNum1()
    val num2 = getNum2()
    val addedResult = add(num1, num2)
    println("The answer: $addedResult")
}

internal data class Hello(@field:SerializedName("id") val id: Int, @field:SerializedName("content") val content: String)
internal interface Service {
    @GET("greeting")
    fun greeting(): Call<Hello>


}

fun networkCall() = launch {
    println("Call some feeds normally, it needs some sec......")

    val response = getResponse()
    response.takeIf { it.isSuccessful }?.let {
        println("response networkCall: ${it.body()}")
    } ?: kotlin.run {
        println("Something wrong at getting response")
    }

    // Because JVM might end before response coming. We could miss the output.
    // If there're other functions behind this function, we would see output.
    // Otherwise you can delay for some minutes
    // with delay(5, TimeUnit.SECONDS).
    // Better:
    // Or: join() to wait until the child routine completes.
}

fun networkCallAsync() = launch {
    println("Call some feeds with async, it needs some sec......")

    async(CommonPool) {
        val response = getResponse()
        response.takeIf { it.isSuccessful }?.let {
            println("response networkCallAsync: ${it.body()}")
        } ?: kotlin.run {
            println("Something wrong at getting response")
        }
    }.apply { await() }
}

fun repeatFunction() = launch {
    println("Try to play with kotlin builtIn repeat().")
    repeat(1000) { index ->
        println("do at $index").apply { delay(1, TimeUnit.SECONDS) }
    }
}

private suspend fun getNum1() = 50.apply {
    delay(Time1, TimeUnit.SECONDS)
    println("$Time1 sec call")
}

private suspend fun getNum2() = 50.apply {
    delay(Time2, TimeUnit.SECONDS)
    println("$Time2 sec call")
}

private suspend fun getResponse() = Retrofit.Builder().baseUrl("http://rest-service.guides.spring.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build().create(Service::class.java).greeting().execute()
