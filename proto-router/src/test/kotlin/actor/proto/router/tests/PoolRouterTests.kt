package actor.proto.router.tests

import actor.proto.Props
import actor.proto.fromProducer
import actor.proto.requestAwait
import actor.proto.router.Routees
import actor.proto.router.RouterGetRoutees
import actor.proto.router.fixture.DoNothingActor
import actor.proto.router.fixture.TestMailbox
import actor.proto.router.newBroadcastPool
import actor.proto.router.newConsistentHashPool
import actor.proto.router.newRandomPool
import actor.proto.router.newRoundRobinPool
import actor.proto.spawn
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

class PoolRouterTests {
    private val MyActorProps: Props = fromProducer { DoNothingActor() }
    private val _timeout: Duration = Duration.ofMillis(1000)
    @Test
    fun `broadcast group pool, creates routees`() {
        runBlocking {
            val props = newBroadcastPool(MyActorProps, 3).withMailbox { TestMailbox() }
            val router = spawn(props)
            val routees = requestAwait<Routees>(router,RouterGetRoutees, _timeout)
            assertEquals(3, routees.pids.count())
        }
    }

    @Test
    fun `round robin pool, creates routees`() {
        runBlocking {
            val props = newRoundRobinPool(MyActorProps, 3).withMailbox { TestMailbox() }
            val router = spawn(props)
            val routees = requestAwait<Routees>(router,RouterGetRoutees, _timeout)
            assertEquals(3, routees.pids.count())
        }
    }

    @Test
    fun `consistent hash pool, creates routees`() {
        runBlocking {
            val props = newConsistentHashPool(MyActorProps, 3, { 0 }, 1).withMailbox { TestMailbox() }
            val router = spawn(props)
            val routees = requestAwait<Routees>(router,RouterGetRoutees, _timeout)
            assertEquals(3, routees.pids.count())
        }
    }

    @Test
    fun `random pool, creates routees`() {
        runBlocking {
            val props = newRandomPool(MyActorProps, 3, 0).withMailbox { TestMailbox() }
            val router = spawn(props)
            val routees = requestAwait<Routees>(router,RouterGetRoutees, _timeout)
            assertEquals(3, routees.pids.count())
        }
    }
}

