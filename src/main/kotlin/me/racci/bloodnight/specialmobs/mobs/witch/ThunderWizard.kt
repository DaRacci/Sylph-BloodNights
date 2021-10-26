package me.racci.bloodnight.specialmobs.mobs.witch

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractWitch
import org.bukkit.Particle
import org.bukkit.entity.Witch
import org.bukkit.event.entity.ProjectileLaunchEvent

class ThunderWizard(witch: Witch) : AbstractWitch(witch) {

    override fun tick() {
        SpecialMobUtil.spawnParticlesAround(baseEntity, Particle.SPELL_INSTANT, 15)
        if (canShoot(4) && baseEntity.target != null) {
            baseEntity.location.world.strikeLightning(baseEntity.target!!.location)
            shot()
        }
    }

    override fun onProjectileShoot(event: ProjectileLaunchEvent) {
        event.isCancelled = true
    }
}