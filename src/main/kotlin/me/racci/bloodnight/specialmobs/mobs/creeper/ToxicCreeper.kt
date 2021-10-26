package me.racci.bloodnight.specialmobs.mobs.creeper

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.effects.PotionCloud
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractCreeper
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.entity.Creeper
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

class ToxicCreeper(creeper: Creeper) : AbstractCreeper(creeper) {

    override fun tick() {
        SpecialMobUtil.spawnParticlesAround(
            baseEntity.location, Particle.REDSTONE,
            Particle.DustOptions(Color.GREEN, 2f), 5
        )
    }

    override fun onExplosionEvent(event: EntityExplodeEvent) {
        PotionCloud.builder(event.location.subtract(0.0, 1.0, 0.0))
            .fromSource(event.entity as Creeper)
            .setDuration(10)
            .setRadiusPerTick(0.01f)
            .ofColor(Color.GREEN)
            .setPotionType(PotionData(PotionType.POISON, false, true))
    }

    init {
        maxFuseTicks = 20
    }
}