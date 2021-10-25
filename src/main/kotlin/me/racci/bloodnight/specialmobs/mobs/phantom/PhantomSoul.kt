package me.racci.bloodnight.specialmobs.mobs.phantom

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractPhantom

class PhantomSoul(phantom: Phantom?) : AbstractPhantom(phantom) {
    fun tick() {
        SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.GLOWING, 1, true)
        SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.INVISIBILITY, 1, false)
    }

    fun onDamage(event: EntityDamageEvent) {
        if (event.getEntity().getType() == EntityType.PLAYER) {
            (event.getEntity() as LivingEntity).addPotionEffect(
                PotionEffect(
                    PotionEffectType.BLINDNESS,
                    10,
                    1,
                    true,
                    true
                )
            )
        }
    }
}