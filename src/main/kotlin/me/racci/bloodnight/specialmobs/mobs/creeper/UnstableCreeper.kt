package me.racci.bloodnight.specialmobs.mobs.creeper

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractCreeper
import org.bukkit.Particle

/**
 * Unstable creeper explodes on damage. Can only be killed by critical attacks.
 */
class UnstableCreeper(creeper: Creeper?) : AbstractCreeper(creeper) {
    override fun tick() {
        SpecialMobUtil.spawnParticlesAround(baseEntity.location, Particle.END_ROD, 2)
    }

    override fun onDamage(event: EntityDamageEvent) {
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            explode()
        }
    }

    init {
        explosionRadius = 10
        isPowered = true
        maxFuseTicks = 50
    }
}