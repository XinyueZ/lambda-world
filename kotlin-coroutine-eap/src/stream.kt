import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    //runChannel()
    runAndCloseChannel()
}

// https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.1/-/blob/coroutines-guide.md#channel-basics
fun runChannel() = runBlocking {
    val channel = Channel<Int>()
    launch {
        (0..100).forEach { it ->
            delay(100)
            channel.send(it)
        }
    }
    (0..100).forEach {_ ->
        print("${channel.receive()} ") //Receive can block(suspend).
    }
    println("\nend")
}

// https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.1/-/blob/coroutines-guide.md#closing-and-iteration-over-channels
fun runAndCloseChannel() = runBlocking{
    val channel = Channel<Int>()
    launch {
        (0..100).forEach { it ->
            delay(100)
            channel.send(it)
        }
        channel.close() //A "close" end-token will be sent and "for" below understands it.
        //If the close() is not called, you will never see "end", the "for" waits end-token forever.
    }

    for( rec in channel){
        print("$rec ") //Receive can block(suspend).
    }
    println("\nend")
}