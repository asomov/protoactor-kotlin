package actor.proto.router

import actor.proto.*
import java.util.concurrent.CountDownLatch


class PoolRouterActor(private val routeeProps: Props, private val config: PoolRouterConfig, private val routerState: RouterState, private val wg: CountDownLatch) : Actor {
    suspend override fun receive(context: Context) {
        val message = context.message
        when (message) {
            is Started -> {
                config.onStarted(context, routeeProps, routerState)
                wg.countDown()
            }
            is RouterAddRoutee -> {
                val r = routerState.getRoutees()
                if (!r.contains(message.pid)) {
                    context.watch(message.pid)
                    routerState.setRoutees(r + message.pid)
                }
            }
            is RouterRemoveRoutee -> {
                val r = routerState.getRoutees()
                if (r.contains(message.pid)) {
                    context.unwatch(message.pid)
                    routerState.setRoutees(r - message.pid)
                }
            }
            is RouterBroadcastMessage -> routerState.getRoutees().forEach { it.request(message, context.sender!!) }
            is RouterGetRoutees -> context.sender!!.send(Routees(routerState.getRoutees()))
        }
    }
}
