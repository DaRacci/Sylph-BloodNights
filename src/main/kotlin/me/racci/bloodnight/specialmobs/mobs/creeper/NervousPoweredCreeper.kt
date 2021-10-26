package me.racci.bloodnight.specialmobs.mobs.creeper

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractCreeper
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.entity.Creeper
import org.bukkit.potion.PotionEffectType

class NervousPoweredCreeper(creeper: Creeper) : AbstractCreeper(creeper) {

    override fun tick() {
        SpecialMobUtil.addPotionEffect(baseEntity, PotionEffectType.SPEED, 2, false)
    }

    override fun onEnd() {
        maxFuseTicks = 0
        ignite()
    }

    init {
        isPowered = true
        maxFuseTicks = 1
        SpecialMobUtil.spawnParticlesAround(
            baseEntity.location, Particle.REDSTONE,
            Particle.DustOptions(Color.RED, 5f), 10
        )
    }
}