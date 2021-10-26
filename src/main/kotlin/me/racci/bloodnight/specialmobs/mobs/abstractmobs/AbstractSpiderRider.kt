package me.racci.bloodnight.specialmobs.mobs.abstractmobs

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.StatSource
import me.racci.bloodnight.specialmobs.mobs.ExtendedSpecialMob
import org.bukkit.Particle
import org.bukkit.entity.Mob
import org.bukkit.event.entity.EntityDamageEvent

abstract class AbstractSpiderRider(carrier: Mob, passenger: Mob) :
    ExtendedSpecialMob<Mob, Mob>(carrier, passenger, StatSource.CARRIER) {

    override fun tick() {
        if (baseEntity.isDead || !baseEntity.isValid) {
            remove()
        }
    }

    override fun onExtensionDamage(event: EntityDamageEvent) {
        if (event.cause == EntityDamageEvent.DamageCause.SUFFOCATION) {
            event.isCancelled = true
            return
        }
        super.onExtensionDamage(event)
    }

    override fun onEnd() {
        SpecialMobUtil.spawnParticlesAround(baseEntity, Particle.CAMPFIRE_COSY_SMOKE, 30)
    }
}