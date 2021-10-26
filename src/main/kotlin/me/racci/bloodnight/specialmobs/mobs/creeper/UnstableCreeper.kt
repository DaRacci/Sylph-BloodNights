package me.racci.bloodnight.specialmobs.mobs.creeper

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractCreeper
import org.bukkit.Particle
import org.bukkit.entity.Creeper
import org.bukkit.event.entity.EntityDamageEvent

/**
 * Unstable creeper explodes on damage. Can only be killed by critical attacks.
 */
class UnstableCreeper(creeper: Creeper) : AbstractCreeper(creeper) {

    override fun tick() {
        SpecialMobUtil.spawnParticlesAround(baseEntity.location, Particle.END_ROD, 2)
    }

    override fun onDamage(event: EntityDamageEvent) {
        if (event.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.cause == EntityDamageEvent.DamageCause.PROJECTILE) {
            explode()
        }
    }

    init {
        explosionRadius = 10
        isPowered = true
        maxFuseTicks = 50
    }
}