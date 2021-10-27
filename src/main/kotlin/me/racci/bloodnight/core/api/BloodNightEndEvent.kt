package me.racci.bloodnight.core.api

import org.bukkit.World
import org.bukkit.event.HandlerList
import org.bukkit.event.world.WorldEvent

/**
 * Event which is fired when a blood nights ends.
 */
class BloodNightEndEvent(world: World) : WorldEvent(world) {

    override fun getHandlers() = handlerList

    companion object {
        private val handlerList = org.bukkit.event.HandlerList()

        @JvmStatic
        fun getHandlerList() = handlerList
    }
}