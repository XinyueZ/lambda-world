
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    //runChannel()
    //runAndCloseChannel()

//    dataProducer1()
//    print("\n---2nd way---\n")
//    dataProducer2()

    pipeline()
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
    (0..100).forEach { _ ->
        print("${channel.receive()} ") //Receive can block(suspend).
    }
    println("\nend")
}

// https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.1/-/blob/coroutines-guide.md#closing-and-iteration-over-channels
fun runAndCloseChannel() = runBlocking {
    val channel = Channel<Int>()
    launch {
        (0..100).forEach { it ->
            delay(100)
            channel.send(it)
        }
        channel.close() //A "close" end-token will be sent and "for" below understands it.
        //If the close() is not called, you will never see "end", the "for" waits end-token forever.
    }

    for (rec in channel) {
        print("$rec ") //Receive can block(suspend).
    }
    println("\nend")
}

// https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.1/-/blob/coroutines-guide.md#building-channel-producers
fun dataProducer1() = runBlocking {
    val producer = createData()
    producer.consumeEach {
        //consumeEach can block(suspend).
        print("$it ")
    }
    println("\nend")
}

// https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.1/-/blob/coroutines-guide.md#building-channel-producers
fun dataProducer2() = runBlocking {
    val producer = provideData()

    launch {
        producer.consumeEach {
            //consumeEach can block(suspend).
            print("$it ")
        }
        println("\nend")
    }

    println("Output:")
}

private fun CoroutineScope.createData() = this.produce<Int> {
    (0..100).forEach { it ->
        delay(100)
        channel.send(it)
    }
}

private suspend fun provideData() = coroutineScope {
    this.produce<Int> {
        (0..100).forEach { it ->
            delay(100)
            channel.send(it)
        }
    }
}

// https://sourcegraph.com/github.com/Kotlin/kotlinx.coroutines@0.26.1/-/blob/coroutines-guide.md#pipelines
fun pipeline() = runBlocking {
    launch {
        val ds = dataSource()
        val result = mapSquare(ds)
        for (res in result) { //Block(suspend)
            if (res > 500)
                break
            else
                print("$res ")
        }

        //Stop channels:
//        ds.cancel()
//        result.cancel()
        //Reduce to this line
        this.coroutineContext.cancelChildren()
        println("\nend")
    }

    println("Output:")
}

private fun CoroutineScope.mapSquare(ds: ReceiveChannel<Long>) = this.produce<Long> {
    for (d in ds) {
        send(d * d)
    }
}

private fun CoroutineScope.dataSource() = this.produce<Long> {
    (0..99999999999999999).forEach { it ->
        delay(100)
        channel.send(it)
    }
}