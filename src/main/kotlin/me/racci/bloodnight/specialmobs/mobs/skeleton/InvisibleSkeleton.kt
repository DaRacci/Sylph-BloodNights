package me.racci.bloodnight.specialmobs.mobs.skeleton

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractSkeleton
import org.bukkit.entity.Skeleton

class InvisibleSkeleton(skeleton: Skeleton?) : AbstractSkeleton(skeleton) {
    fun tick() {
        SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.INVISIBILITY, 1, false)
    }
}