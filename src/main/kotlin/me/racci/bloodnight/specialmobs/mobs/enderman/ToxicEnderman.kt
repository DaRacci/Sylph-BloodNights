package me.racci.bloodnight.specialmobs.mobs.enderman

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.effects.PotionCloud
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractEnderman
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Enderman
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

class ToxicEnderman(enderman: Enderman?) : AbstractEnderman(enderman) {
    override fun tick() {
        super.tick()
        SpecialMobUtil.spawnParticlesAround(
            getBaseEntity().getLocation(),
            Particle.REDSTONE,
            Particle.DustOptions(Color.GREEN, 2),
            5
        )
    }

    override fun onTeleport(event: EntityTeleportEvent) {
        val from: Location = event.from
        PotionCloud.builder(from.subtract(0.0, 1.0, 0.0))
            .setPotionType(PotionData(PotionType.POISON, false, true))
            .ofColor(Color.GREEN)
            .setDuration(10)
            .withRadius(4)
            .setRadiusPerTick(0.01f)
            .build()
    }
}