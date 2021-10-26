package me.racci.bloodnight.core.mobfactory

import me.racci.bloodnight.config.worldsettings.WorldSettings
import me.racci.bloodnight.config.worldsettings.mobsettings.MobSetting
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.concurrent.ThreadLocalRandom

class WorldMobFactory(private val settings: WorldSettings) {

    private val rand: ThreadLocalRandom = ThreadLocalRandom.current()

    fun getRandomFactory(entity: Entity): MobFactory? {
        if (entity !is LivingEntity) return null

        // Get the group of the mob
        val mobGroup = SpecialMobRegistry.getMobGroup(entity) ?: return null
        val settings: Set<MobSetting> = settings.mobSettings.mobTypes.settings

        // Search filter for factories with active mobs
        val allowedFactories: List<MobFactory> = mobGroup.factories
            .filter { f ->
                settings.first { it.mobName.equals(f.mobName, true) }.active
            }
        return if (allowedFactories.isEmpty()) null else allowedFactories[rand.nextInt(allowedFactories.size)]
    }
}