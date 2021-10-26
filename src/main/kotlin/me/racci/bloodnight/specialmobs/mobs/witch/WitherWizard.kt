package me.racci.bloodnight.specialmobs.mobs.witch

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractWitch
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.EntityType
import org.bukkit.entity.Witch
import org.bukkit.entity.WitherSkull
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.ItemStack

class WitherWizard(witch: Witch) : AbstractWitch(witch) {

    override fun tick() {
        val equipment: EntityEquipment = baseEntity.equipment
        equipment.setItemInMainHand(ItemStack(Material.WITHER_SKELETON_SKULL))
        SpecialMobUtil.spawnParticlesAround(baseEntity, Particle.SPELL_INSTANT, 15)
        if (canShoot(5)) {
            SpecialMobUtil.launchProjectileOnTarget(baseEntity, WitherSkull::class.java, 4.0)
            shot()
        }
    }

    override fun onProjectileShoot(event: ProjectileLaunchEvent) {
        if (event.entity.type == EntityType.WITHER_SKULL) return
        event.isCancelled = true
    }
}