package me.racci.bloodnight.specialmobs.mobs.enderman

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractEnderman
import org.bukkit.Particle
import org.bukkit.entity.Enderman
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class FearfulEnderman(enderman: Enderman) : AbstractEnderman(enderman) {

    override fun tick() {
        super.tick()
        SpecialMobUtil.spawnParticlesAround(baseEntity, Particle.SPELL_WITCH, 10)
    }

    override fun onHit(event: EntityDamageByEntityEvent) {
        if (event.entity.type == EntityType.PLAYER) {
            (event.entity as LivingEntity).addPotionEffect(
                PotionEffect(
                    PotionEffectType.BLINDNESS,
                    7 * 20,
                    1,
                    true,
                    true
                )
            )
        }
    }
}