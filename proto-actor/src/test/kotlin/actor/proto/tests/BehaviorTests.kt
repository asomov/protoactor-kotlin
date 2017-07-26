package actor.proto.tests

import actor.proto.*
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

class BehaviorTests {
    fun spawnActorFromFunc(receive: suspend (Context) -> Unit): PID = spawn(fromFunc(receive))
    @Test fun `can change states`() = runBlocking {
        val testActorProps: Props = fromProducer { LightBulb() }
        val actor: PID = spawn(testActorProps)
        Assert.assertEquals("Turning on", actor.requestAwait<String>(PressSwitch))
        Assert.assertEquals("Hot!", actor.requestAwait<String>(Touch))
        Assert.assertEquals("Turning off", actor.requestAwait<String>(PressSwitch))
        Assert.assertEquals("Cold", actor.requestAwait<String>(Touch))
    }

    @Test fun `can use global behaviour`() = runBlocking {
        val testActorProps: Props = fromProducer { LightBulb() }
        val actor: PID = spawn(testActorProps)
        assertEquals("Turning on", actor.requestAwait(PressSwitch))
        assertEquals("Smashed!", actor.requestAwait(HitWithHammer))
        assertEquals("Broken", actor.requestAwait(PressSwitch))
        assertEquals("OW!", actor.requestAwait(Touch))
    }

    @Test fun `pop behavior should restore pushed behavior`() = runBlocking {
        val behavior: Behavior = Behavior()
        behavior.become { ctx ->
            if (ctx.message is String) {
                behavior.becomeStacked { ctx2 ->
                    ctx2.respond(42)
                    behavior.unbecomeStacked()
                }

                ctx.respond(ctx.message)
            }
        }

        val pid: PID = spawnActorFromFunc({ behavior.receive(it) })
        assertEquals("number", pid.requestAwait("number"))
        assertEquals(42, pid.requestAwait(123))
        assertEquals("answertolifetheuniverseandeverything", pid.requestAwait("answertolifetheuniverseandeverything"))
    }
}


class LightBulb : Actor {
    private val _behavior: Behavior = Behavior()
    private var _smashed: Boolean = false
    private suspend fun off(context: Context) {
        when (context.message) {
            is PressSwitch -> {
                context.respond("Turning on")
                _behavior.become { on(it) }
            }
            is Touch -> {
                context.respond("Cold")
            }
        }
    }

    private suspend fun on(context: Context) {
        when (context.message) {
            is PressSwitch -> {
                context.respond("Turning off")
                _behavior.become { off(it) }
            }
            is Touch -> {
                context.respond("Hot!")
            }
        }
    }

    suspend override fun receive(context: Context) {
        when (context.message) {
            is HitWithHammer -> {
                context.respond("Smashed!")
                _smashed = true
                return
            }
            is PressSwitch -> if (_smashed) {
                context.respond("Broken")
                return
            }
            is Touch -> if (_smashed) {
                context.respond("OW!")
                return
            }
        }
        _behavior.receive(context)
    }

    init {
        _behavior.become { off(it) }
    }
}

object PressSwitch
object Touch
object HitWithHammer