package actor.proto.router.fixture

import actor.proto.mailbox.Dispatcher
import kotlinx.coroutines.experimental.CoroutineScope

class TestDispatcher : Dispatcher {
    override var throughput: Int = 10
    override fun schedule(runner: suspend () -> Unit) {
    }
}

