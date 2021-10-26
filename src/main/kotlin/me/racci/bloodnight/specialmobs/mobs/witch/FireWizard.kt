package me.racci.bloodnight.specialmobs.mobs.witch

import de.eldoria.eldoutilities.utils.ObjUtil
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractWitch
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.entity.*
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.ItemStack

class FireWizard(witch: Witch) : AbstractWitch(witch) {

    override fun tick() {
        val equipment: EntityEquipment = baseEntity.equipment
        equipment.setItemInMainHand(ItemStack(Material.FIRE_CHARGE))
        SpecialMobUtil.spawnParticlesAround(baseEntity, Particle.DRIP_LAVA, 5)
        if (canShoot(5)) {
            SpecialMobUtil.launchProjectileOnTarget(baseEntity, LargeFireball::class.java, 4.0)
            shot()
        }
    }

    override fun onProjectileShoot(event: ProjectileLaunchEvent) {
        if (event.entity.type == EntityType.FIREBALL) return
        event.isCancelled = true
    }

    override fun onProjectileHit(event: ProjectileHitEvent) {
        ObjUtil.nonNull<Projectile>(event.entity) {
            if (it.type == EntityType.PLAYER) {
                val attribute: AttributeInstance = baseEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!
                (event.hitEntity as LivingEntity).damage(attribute.value, baseEntity)
            }
        }
    }

    init {
        tick()
    }
}