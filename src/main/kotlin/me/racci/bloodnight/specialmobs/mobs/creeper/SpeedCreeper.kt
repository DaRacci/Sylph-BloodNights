package me.racci.bloodnight.specialmobs.mobs.creeper

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractCreeper

class SpeedCreeper(creeper: Creeper?) : AbstractCreeper(creeper) {
    override fun tick() {
        SpecialMobUtil.addPotionEffect(baseEntity, PotionEffectType.SPEED, 4, true)
    }

    override fun onEnd() {
        maxFuseTicks = 0
        ignite()
    }

    override fun onExplosionPrimeEvent(event: ExplosionPrimeEvent) {
        event.setFire(true)
    }

    init {
        maxFuseTicks = 10
    }
}