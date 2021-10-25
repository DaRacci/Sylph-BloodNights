package me.racci.bloodnight.specialmobs.mobs.enderman

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractEnderman
import org.bukkit.Particle

class FearfulEnderman(enderman: Enderman?) : AbstractEnderman(enderman) {
    override fun tick() {
        super.tick()
        SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.SPELL_WITCH, 10)
    }

    fun onHit(event: EntityDamageByEntityEvent) {
        if (event.getEntity().getType() == EntityType.PLAYER) {
            (event.getEntity() as LivingEntity).addPotionEffect(
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