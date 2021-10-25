package me.racci.bloodnight.specialmobs.mobs.abstractmobs

import me.racci.bloodnight.specialmobs.SpecialMob
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.entity.*

abstract class AbstractEnderman(enderman: Enderman) : SpecialMob<Enderman>(enderman) {

    override fun tick() {
        if (baseEntity.target != null) return
        val nearbyPlayers: Collection<Entity> =
            baseEntity.world.getNearbyEntities(baseEntity.location, 16.0, 16.0, 16.0) {
                (it.type == EntityType.PLAYER
                        && (it as Player).gameMode == GameMode.SURVIVAL)
            }
        if (nearbyPlayers.isEmpty()) {
            return
        }
        baseEntity.target = ArrayList(nearbyPlayers)[0] as LivingEntity
    }

    override fun onEnd() {
        SpecialMobUtil.spawnParticlesAround(baseEntity, Particle.DRAGON_BREATH, 10)
    }
}