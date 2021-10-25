package me.racci.bloodnight.specialmobs.mobs.phantom

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractPhantom

class FearfulPhantom(phantom: Phantom?) : AbstractPhantom(phantom) {
    fun tick() {
        SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.GLOWING, 1, true)
    }

    fun onHit(event: EntityDamageByEntityEvent) {
        if (event.getEntity().getType() == EntityType.PLAYER) {
            (event.getEntity() as LivingEntity).addPotionEffect(
                PotionEffect(
                    PotionEffectType.SLOW,
                    7 * 20,
                    2,
                    true,
                    true
                )
            )
        }
    }
}