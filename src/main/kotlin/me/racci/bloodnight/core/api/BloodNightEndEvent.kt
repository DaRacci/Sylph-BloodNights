package me.racci.bloodnight.core.api

import org.bukkit.World
import org.bukkit.event.HandlerList
import org.bukkit.event.world.WorldEvent

/**
 * Event which is fired when a blood nights ends.
 */
class BloodNightEndEvent(world: World) : WorldEvent(world) {

    companion object {
        private val HANDLERS: HandlerList = HandlerList()
        val handlerList: HandlerList
            get() = HANDLERS
    }

    override fun getHandlers() = HANDLERS
}