package me.racci.bloodnight.specialmobs.mobs.abstractmobs

import me.racci.bloodnight.specialmobs.SpecialMob
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import org.bukkit.Particle
import org.bukkit.entity.Skeleton
import org.bukkit.entity.Stray

abstract class AbstractSkeleton(skeleton: Skeleton) : SpecialMob<Skeleton>(skeleton) {

    override fun onEnd() {
        SpecialMobUtil.spawnParticlesAround(baseEntity, Particle.CAMPFIRE_COSY_SMOKE, 30)
    }
}

abstract class AbstractStray(stray: Stray) : SpecialMob<Stray>(stray)