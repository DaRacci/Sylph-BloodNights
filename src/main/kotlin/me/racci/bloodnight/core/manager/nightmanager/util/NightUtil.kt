package me.racci.bloodnight.core.manager.nightmanager.util

import de.eldoria.eldoutilities.utils.EMath
import me.racci.bloodnight.config.worldsettings.NightSettings
import me.racci.bloodnight.config.worldsettings.WorldSettings
import org.bukkit.World
import kotlin.math.floor

object NightUtil {

    fun getMoonPhase(world: World): Int {
        val days = floor(world.fullTime / 24000.0).toInt()
        return days % 8
    }

    /**
     * Get the progress of the night
     *
     * @param world         world which refers to the settings
     * @param worldSettings settings of the world
     * @return the night progress where 0 ist the end and 1 ist the start.
     */
    fun getNightProgress(world: World, worldSettings: WorldSettings): Double {
        val settings: NightSettings = worldSettings.nightSettings
        val total = getDiff(settings.nightBegin.toLong(), settings.nightEnd.toLong())
        val left = getDiff(world.fullTime, settings.nightEnd.toLong())
        return EMath.clamp(0.0, 1.0, left / total.toDouble())
    }

    fun getSecondsRemaining(world: World, worldSettings: WorldSettings): Int {
        val ns: NightSettings = worldSettings.nightSettings
        val nightSeconds: Int = ns.currentNightDuration / 20
        return (nightSeconds * getNightProgress(world, worldSettings)).toInt()
    }

    fun getNightTicksPerTick(world: World, worldSettings: WorldSettings): Double {
        val ns: NightSettings = worldSettings.nightSettings
        val nightDurationTicks: Long = ns.currentNightDuration.toLong()
        val normalTicks = getDiff(ns.nightBegin.toLong(), ns.nightEnd.toLong())
        return normalTicks.toDouble() / nightDurationTicks
    }

    fun getDiff(fullTime: Long, nextTime: Long): Long {
        val currentTime = fullTime % 24000
        return if (currentTime > nextTime) 24000 - currentTime + nextTime else nextTime - currentTime
    }

    fun isNight(world: World, worldSettings: WorldSettings): Boolean {
        val openInTicks = getDiff(world.fullTime, worldSettings.nightSettings.nightBegin.toLong())
        val closedInTicks = getDiff(world.fullTime, worldSettings.nightSettings.nightEnd.toLong())
        return openInTicks > closedInTicks
    }
}