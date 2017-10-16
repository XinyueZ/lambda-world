fun main(args: Array<String>) {
    csharp()
}

interface IEventHandler {
    fun getSender(): Any?
    operator fun plusAssign(eventHandler: IEventHandler)
    operator fun minusAssign(eventHandler: IEventHandler)
}

class EventHandler(private val sender: Any? = null) : IEventHandler {
    private var eventHandler: IEventHandler? = null

    operator override fun plusAssign(eventHandler: IEventHandler) {
        this.eventHandler = eventHandler
    }

    operator override fun minusAssign(eventHandler: IEventHandler) {
        this.eventHandler = null
    }

    override fun getSender() = sender

    override fun toString() = eventHandler?.getSender()?.toString() ?: kotlin.run { "no event-handler found" }
}

class Foo {
    val click: IEventHandler = EventHandler()
    override fun toString() = "Foo as sender"
}

private fun csharp() {
    logln("Pretend to have C# event-handler")
    with(Foo()) {
        EventHandler(this).apply {
            click += this
            println("Fire event: $click")
            click -= this
            println("Fire event: $click")
        }
    }
}