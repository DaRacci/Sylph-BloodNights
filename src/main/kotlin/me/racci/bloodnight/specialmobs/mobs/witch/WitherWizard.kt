package me.racci.bloodnight.specialmobs.mobs.witch

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractWitch
import org.bukkit.Particle

class WitherWizard(witch: Witch?) : AbstractWitch(witch) {
    fun tick() {
        val equipment: EntityEquipment = getBaseEntity().getEquipment()
        equipment.setItemInMainHand(ItemStack(Material.WITHER_SKELETON_SKULL))
        SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.SPELL_INSTANT, 15)
        if (canShoot(5)) {
            SpecialMobUtil.launchProjectileOnTarget(getBaseEntity(), WitherSkull::class.java, 4)
            shot()
        }
    }

    fun onProjectileShoot(event: ProjectileLaunchEvent) {
        if (event.getEntity().getType() == EntityType.WITHER_SKULL) return
        event.setCancelled(true)
    }
}