package me.racci.bloodnight.specialmobs.mobs.slime

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractSlime
import org.bukkit.entity.Slime
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class ToxicSlime(slime: Slime?) : AbstractSlime(slime) {
    fun onDeath(event: EntityDeathEvent) {
        SpecialMobUtil.spawnLingeringPotionAt(
            event.getEntity().getLocation(),
            PotionEffect(PotionEffectType.POISON, 5 * 20, 2, true, true)
        )
    }
}