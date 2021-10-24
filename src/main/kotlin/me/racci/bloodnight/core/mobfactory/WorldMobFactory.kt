package me.racci.bloodnight.core.mobfactory

import de.eldoria.bloodnight.config.worldsettings.WorldSettings
import me.racci.bloodnight.config.worldsettings.WorldSettings
import me.racci.bloodnight.config.worldsettings.mobsettings.MobSetting
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Predicate
import java.util.stream.Collectors

class WorldMobFactory(settings: WorldSettings) {

    private val settings: WorldSettings
    private val rand: ThreadLocalRandom = ThreadLocalRandom.current()

    fun getRandomFactory(entity: Entity): MobFactory? {
        if (entity !is LivingEntity) return null

        // Get the group of the mob
        val optionalMobGroup = SpecialMobRegistry.getMobGroup(entity) ?: return null
        val mobGroup = optionalMobGroup.get()
        val settings: Set<MobSetting> = settings.getMobSettings().getMobTypes().getSettings()

        // Search filter for factories with active mobs
        val allowedFactories: List<MobFactory> = mobGroup.getFactories().stream()
            .filter { factory ->
                settings.stream() // search for setting for factory
                    .filter(Predicate<MobSetting> { setting: MobSetting ->
                        setting.getMobName().equalsIgnoreCase(factory.getMobName())
                    }) // take first
                    .findFirst() // draw active value or false
                    .map<Any>(MobSetting::isActive)
                    .orElse(false)
            }
            .collect(Collectors.toList())
        return if (allowedFactories.isEmpty()) Optional.empty() else Optional.of(
            allowedFactories[rand.nextInt(allowedFactories.size)]
        )
    }

    init {
        this.settings = settings
    }
}