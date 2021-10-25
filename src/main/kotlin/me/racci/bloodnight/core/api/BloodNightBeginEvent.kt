package me.racci.bloodnight.core.api

import org.bukkit.World
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.world.WorldEvent

/**
 * Event which is fired when a blood nights begins.
 *
 *
 * This event is [Cancellable]. If the event is canceled, no Blood Night will be initialized.
 */
class BloodNightBeginEvent(world: World) : WorldEvent(world), Cancellable {

    private var cancelled = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    companion object {
        private val HANDLERS: HandlerList = HandlerList()
        val handlerList: HandlerList
            get() = HANDLERS
    }

    override fun getHandlers() =
        HANDLERS
}