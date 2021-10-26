package me.racci.bloodnight.core.api

import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.config.worldsettings.NightSelection
import me.racci.bloodnight.core.manager.nightmanager.NightManager
import me.racci.bloodnight.core.manager.nightmanager.util.NightUtil
import org.bukkit.World

/**
 * Provides safe to use methods to interact with Blood Night.
 *
 * @since 0.8
 */
class BloodNightAPI(nightManager: NightManager, configuration: Configuration) : IBloodNightAPI {

    private val nightManager: NightManager
    private val configuration: Configuration

    override fun isBloodNightActive(world: World) =
        nightManager.isBloodNightActive(world)

    override fun forceNight(world: World) {
        nightManager.forceNight(world)
    }

    override fun cancelNight(world: World) {
        nightManager.cancelNight(world)
    }

    override val bloodWorlds: HashSet<World>
        get() = nightManager.bloodWorldsSet

    override fun getSecondsLeft(world: World): Int {
        return if (!isBloodNightActive(world)) 0 else NightUtil.getSecondsRemaining(
            world,
            configuration.getWorldSettings(world)
        )
    }

    override fun getPercentLeft(world: World): Double {
        return if (!isBloodNightActive(world)) 0.0 else NightUtil.getNightProgress(
            world,
            configuration.getWorldSettings(world)
        ) * 100
    }

    override fun nextProbability(world: World, offset: Int): Int {
        val ns: NightSelection = configuration.getWorldSettings(world).nightSelection
        return ns.getNextProbability(world, offset)
    }

    init {
        this.nightManager = nightManager
        this.configuration = configuration
    }
}