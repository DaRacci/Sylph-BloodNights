package me.racci.bloodnight.specialmobs.mobs.creeper

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractCreeper
import org.bukkit.Color
import org.bukkit.Particle

class ToxicCreeper(creeper: Creeper?) : AbstractCreeper(creeper) {
    override fun tick() {
        SpecialMobUtil.spawnParticlesAround(baseEntity.location, Particle.REDSTONE, DustOptions(Color.GREEN, 2), 5)
    }

    override fun onExplosionEvent(event: EntityExplodeEvent) {
        PotionCloud.builder(event.getLocation().subtract(0.0, 1.0, 0.0))
            .fromSource(event.getEntity() as Creeper)
            .setDuration(10)
            .setRadiusPerTick(0.01f)
            .ofColor(Color.GREEN)
            .setPotionType(PotionData(PotionType.POISON, false, true))
    }

    init {
        maxFuseTicks = 20
    }
}