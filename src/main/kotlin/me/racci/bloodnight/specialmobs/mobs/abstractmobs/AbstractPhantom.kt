package me.racci.bloodnight.specialmobs.mobs.abstractmobs

import me.racci.bloodnight.specialmobs.SpecialMob
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.StatSource
import me.racci.bloodnight.specialmobs.mobs.ExtendedSpecialMob
import org.bukkit.Particle
import org.bukkit.entity.Mob
import org.bukkit.entity.Phantom
import org.bukkit.event.entity.EntityDamageEvent

abstract class AbstractPhantom protected constructor(phantom: Phantom) : SpecialMob<Phantom>(phantom) {

    override fun onEnd() {
        SpecialMobUtil.spawnParticlesAround(baseEntity, Particle.CAMPFIRE_COSY_SMOKE, 30)
    }
}

abstract class AbstractExtendedPhantom<T : Mob, U : Mob>(carrier: T, passenger: U) :
    ExtendedSpecialMob<T, U>(carrier, passenger, StatSource.CARRIER) {

    override fun tick() {
        if (!baseEntity.isValid || !passenger.isValid) remove()
    }

    override fun onExtensionDamage(event: EntityDamageEvent) {
        if (event.cause == EntityDamageEvent.DamageCause.SUFFOCATION) event.isCancelled = true
    }

}