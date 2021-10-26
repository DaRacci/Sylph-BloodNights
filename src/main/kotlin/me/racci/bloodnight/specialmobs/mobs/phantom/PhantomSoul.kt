package me.racci.bloodnight.specialmobs.mobs.phantom

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractPhantom
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Phantom
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class PhantomSoul(phantom: Phantom) : AbstractPhantom(phantom) {

    override fun tick() {
        SpecialMobUtil.addPotionEffect(baseEntity, PotionEffectType.GLOWING, 1, true)
        SpecialMobUtil.addPotionEffect(baseEntity, PotionEffectType.INVISIBILITY, 1, false)
    }

    override fun onDamage(event: EntityDamageEvent) {
        if (event.entity.type == EntityType.PLAYER) {
            (event.entity as LivingEntity).addPotionEffect(
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