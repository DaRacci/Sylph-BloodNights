package me.racci.bloodnight.specialmobs.mobs.spider

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractSpiderRider
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import org.bukkit.potion.PotionEffectType

class WitherSkeletonRider(carrier: Mob) :
    AbstractSpiderRider(carrier, SpecialMobUtil.spawnAndMount(carrier, EntityType.WITHER_SKELETON)) {

    override fun tick() {
        SpecialMobUtil.addPotionEffect(baseEntity, PotionEffectType.SPEED, 1, true)
    }
}