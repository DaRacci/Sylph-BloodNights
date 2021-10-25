package me.racci.bloodnight.specialmobs.mobs.spider

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractSpiderRider

class SpeedSkeletonRider(carrier: Mob?) :
    AbstractSpiderRider(carrier, SpecialMobUtil.spawnAndMount(carrier, EntityType.SKELETON)) {
    override fun tick() {
        SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.SPEED, 1, true)
    }
}