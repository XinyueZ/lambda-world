class EventHandler(val name: String)

class Foo {
    private var eventHandler: EventHandler? = null

    operator fun plusAssign(eventHandler: EventHandler) {
        this.eventHandler = eventHandler
    }

    operator fun minusAssign(eventHandler: EventHandler) {
        this.eventHandler = null
    }

    override fun toString(): String {
        return eventHandler?.let { it.name } ?: kotlin.run { "no name" }
    }
}

fun main(args: Array<String>) {
    println("hello, operators")

    val ev = EventHandler("Event")
    val foo = Foo()
    foo += ev

    println("$foo")

    foo -= ev

    println("$foo")
}