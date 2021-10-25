package me.racci.bloodnight.specialmobs.mobs.witch

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractWitch
import org.bukkit.Particle

class ThunderWizard(witch: Witch?) : AbstractWitch(witch) {
    fun tick() {
        SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.SPELL_INSTANT, 15)
        if (canShoot(4) && getBaseEntity().getTarget() != null) {
            getBaseEntity().getLocation().getWorld().strikeLightning(getBaseEntity().getTarget().getLocation())
            shot()
        }
    }

    fun onProjectileShoot(event: ProjectileLaunchEvent) {
        event.setCancelled(true)
    }
}