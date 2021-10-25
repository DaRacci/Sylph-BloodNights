package me.racci.bloodnight.core.manager.nightmanager

import me.racci.bloodnight.config.Configuration
import org.bukkit.scheduler.BukkitRunnable

class SoundManager(private val nightManager: NightManager, configuration: Configuration) : BukkitRunnable() {

    private val configuration: Configuration
    override fun run() {
        playRandomSound()
    }

    private fun playRandomSound() {
        for (data in nightManager.bloodWorldsMap.values) {
            data.playRandomSound(configuration.getWorldSettings(data.world).soundSettings)
        }
    }

    init {
        this.configuration = configuration
    }
}