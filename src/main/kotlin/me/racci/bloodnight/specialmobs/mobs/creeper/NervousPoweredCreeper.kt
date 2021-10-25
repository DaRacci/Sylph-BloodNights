package me.racci.bloodnight.specialmobs.mobs.creeper

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractCreeper
import org.bukkit.Color
import org.bukkit.Particle

class NervousPoweredCreeper(creeper: Creeper?) : AbstractCreeper(creeper) {
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
        SpecialMobUtil.spawnParticlesAround(baseEntity.location, Particle.REDSTONE, DustOptions(Color.RED, 5), 10)
    }
}