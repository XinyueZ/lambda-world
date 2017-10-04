import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.experimental.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

const val Time1 = 2L
const val Time2 = 5L
val sumTime = Time1 + Time2

fun add(x: Int, y: Int) = x + y

fun main(args: Array<String>) {
    doSomethingAsync(false)
    doSomethingAsync(true)
    networkCall()
    networkCallAsync()
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
        println("Completed noDependency in $time ms")
    }
}

fun dependency() {
    println("Call two functions which depend each other, need about $sumTime  seconds.......")

    runBlocking {
        val time = measureTimeMillis {
            launch {
                val num1 = getNum1()
                val num2 = getNum2()
                val addedResult = add(num1, num2)
                println("The answer: $addedResult")
            }.apply { join() }
        }
        println("Completed dependency in $time ms")
    }
}

internal data class Hello(@field:SerializedName("id") val id: Int, @field:SerializedName("content") val content: String)
internal interface Service {
    @GET("greeting")
    fun greeting(): Call<Hello>


}

fun networkCall() = runBlocking {
    println("Call some feeds normally, it needs some sec......")

    val time = measureTimeMillis {
        launch {
            val service = Retrofit.Builder().baseUrl("http://rest-service.guides.spring.io/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build().create(Service::class.java)
            val response = service.greeting().execute()
            response.takeIf { it.isSuccessful }?.let {
                println("response networkCall: ${it.body()}")
            } ?: kotlin.run {
                println("Something wrong at getting response")
            }
        }.apply { join() }

        // Because JVM might end before response coming. We could miss the output.
        // If there're other functions behind this function, we would see output.
        // Otherwise you can delay for some minutes
        // with delay(5, TimeUnit.SECONDS).
        // Better:
        // Or: join() to wait until the child routine completes.

    }
    println("Completed networkCall in $time ms")
}

fun networkCallAsync() = runBlocking {
    println("Call some feeds with async, it needs some sec......")

    val time = measureTimeMillis {
        async(CommonPool) {
            val service = Retrofit.Builder().baseUrl("http://rest-service.guides.spring.io/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build().create(Service::class.java)
            val response = service.greeting().execute()
            response.takeIf { it.isSuccessful }?.let {
                println("response networkCallAsync: ${it.body()}")
            } ?: kotlin.run {
                println("Something wrong at getting response")
            }
        }.apply { await() }
    }
    println("Completed networkCallAsync in $time ms")
}