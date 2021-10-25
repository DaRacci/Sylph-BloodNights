package me.racci.bloodnight.specialmobs.mobs.abstractmobs

import me.racci.bloodnight.specialmobs.SpecialMob
import org.bukkit.entity.Creeper

abstract class AbstractCreeper(creeper: Creeper) : SpecialMob<Creeper>(creeper) {

    override fun onEnd() {}

    var maxFuseTicks: Int
        get() = baseEntity.maxFuseTicks
        set(fuse) {
            baseEntity.maxFuseTicks = fuse
        }
    var explosionRadius: Int
        get() = baseEntity.explosionRadius
        set(radius) {
            baseEntity.explosionRadius = radius
        }

    fun explode() {
        baseEntity.explode()
    }

    fun ignite() {
        baseEntity.ignite()
    }

    var isPowered: Boolean
        get() = baseEntity.isPowered
        set(value) {
            baseEntity.isPowered = value
        }
}