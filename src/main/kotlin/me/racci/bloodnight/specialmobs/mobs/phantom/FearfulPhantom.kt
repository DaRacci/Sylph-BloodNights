package me.racci.bloodnight.specialmobs.mobs.phantom

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractPhantom
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Phantom
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class FearfulPhantom(phantom: Phantom) : AbstractPhantom(phantom) {

    override fun tick() {
        SpecialMobUtil.addPotionEffect(baseEntity, PotionEffectType.GLOWING, 1, true)
    }

    override fun onHit(event: EntityDamageByEntityEvent) {
        if (event.entity.type == EntityType.PLAYER) {
            (event.entity as LivingEntity).addPotionEffect(
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