package me.racci.bloodnight.specialmobs.mobs.skeleton

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractSkeleton
import org.bukkit.entity.Skeleton
import org.bukkit.potion.PotionEffectType

class InvisibleSkeleton(skeleton: Skeleton) : AbstractSkeleton(skeleton) {

    override fun tick() {
        SpecialMobUtil.addPotionEffect(baseEntity, PotionEffectType.INVISIBILITY, 1, false)
    }
}