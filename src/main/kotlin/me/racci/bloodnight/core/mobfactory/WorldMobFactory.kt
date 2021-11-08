package me.racci.bloodnight.core.mobfactory

import me.racci.bloodnight.config.worldsettings.WorldSettings
import me.racci.bloodnight.config.worldsettings.mobsettings.MobSetting
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.concurrent.ThreadLocalRandom

class WorldMobFactory(private val settings: WorldSettings) {

    private val rand: ThreadLocalRandom = ThreadLocalRandom.current()

    /*private val hollowsEveMobs = arrayListOf(
        SpecialMobRegistry.getMobFactoryByName(HollowAdventurer::class.simpleName!!),   // 0
        SpecialMobRegistry.getMobFactoryByName(HollowArcher::class.simpleName!!),       // 1
        SpecialMobRegistry.getMobFactoryByName(HollowGoliath::class.simpleName!!),      // 2
        SpecialMobRegistry.getMobFactoryByName(HollowNecromancer::class.simpleName!!),  // 3
        SpecialMobRegistry.getMobFactoryByName(HollowHarbinger::class.simpleName!!),    // 4
        SpecialMobRegistry.getMobFactoryByName(HollowHaunter::class.simpleName!!),      // 5
    )*/

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

    /*fun getHollowsEveFactory(entity: Entity) : MobFactory? {
        if(entity !is LivingEntity) return null
        if(entity is Flying) return if(25 < rand.nextInt()) hollowsEveMobs[5] else null
        return when(rand.nextInt(101)) {
            in 0..86 -> {
                if(65 < rand.nextInt(101)) hollowsEveMobs[0] else hollowsEveMobs[1]
            }
            in 86..99 -> {
                if(65 < rand.nextInt(101)) hollowsEveMobs[2] else hollowsEveMobs[3]
            }
            else -> {
                hollowsEveMobs[4]
            }
        }
    }*/
}