package me.racci.bloodnight.core.manager.nightmanager

import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.config.worldsettings.NightSettings
import me.racci.bloodnight.config.worldsettings.WorldSettings
import me.racci.bloodnight.core.manager.nightmanager.util.NightUtil
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.world.TimeSkipEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.roundToInt

class TimeManager(val configuration: Configuration, val nightManager: NightManager) : BukkitRunnable(), Listener {

    private var ignoreSkip = false

    /**
     * Map contains for every active world a boolean if it is currently night.
     */
    private val timeState = HashMap<String, Boolean>()
    private val customTimes = HashMap<String, Double>()

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        calculateWorldState(event.world)
    }
    // <--- Time consistency ---> //
    /**
     * Recalculate time state for immediate impact
     *
     * @param event time skip event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onTimeSkip(event: TimeSkipEvent) {
        if (ignoreSkip) {
            ignoreSkip = false
            return
        }
        customTimes.computeIfPresent(
            event.world.name
        ) { _, v -> v + event.skipAmount }
    }

    override fun run() {
        for (observedWorld in Bukkit.getWorlds()) {
            calculateWorldState(observedWorld)
        }
        refreshTime()
    }

    private fun calculateWorldState(world: World) {
        val current: Boolean = NightUtil.isNight(world, configuration.getWorldSettings(world))
        val old = timeState.getOrDefault(world.name, !current)
        if (current == old) {
            return
        }
        timeState[world.name] = current
        if (current) {
            // A new night has begun.
            nightManager.startNight(world)
            return
        }
        if (nightManager.isBloodNightActive(world)) {
            // A blood night has ended.
            nightManager.endNight(world)
        }
    }

    private fun refreshTime() {
        for ((world, bloodNightData) in nightManager.bloodWorldsMap) {
            val settings: WorldSettings = configuration.getWorldSettings(world.name)
            val ns: NightSettings = settings.nightSettings
            if (ns.isCustomNightDuration) {
                val calcTicks: Double = NightUtil.getNightTicksPerTick(world, settings)
                val time = customTimes.compute(
                    world.name
                ) { _, old -> (old ?: world.fullTime.toDouble()) + calcTicks }
                val newTime = time?.roundToInt()
                if (world.fullTime.toInt() != newTime) {
                    ignoreSkip = true
                    world.fullTime = newTime!!.toLong()
                }
            }
            bloodNightData.bossBar?.progress = NightUtil.getNightProgress(world, configuration.getWorldSettings(world))
        }
    }

    fun removeCustomTime(world: World) {
        customTimes.remove(world.name)
    }

    fun reload() {
        timeState.clear()
    }
}