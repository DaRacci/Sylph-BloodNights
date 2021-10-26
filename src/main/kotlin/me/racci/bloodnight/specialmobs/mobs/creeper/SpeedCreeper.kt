package me.racci.bloodnight.specialmobs.mobs.creeper

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractCreeper
import org.bukkit.entity.Creeper
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.potion.PotionEffectType

class SpeedCreeper(creeper: Creeper) : AbstractCreeper(creeper) {

    override fun tick() {
        SpecialMobUtil.addPotionEffect(baseEntity, PotionEffectType.SPEED, 4, true)
    }

    override fun onEnd() {
        maxFuseTicks = 0
        ignite()
    }

    override fun onExplosionPrimeEvent(event: ExplosionPrimeEvent) {
        event.fire = true
    }

    init {
        maxFuseTicks = 10
    }
}