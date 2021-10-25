package me.racci.bloodnight.specialmobs.mobs.witch

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractWitch
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import java.util.function.Consumer

class FireWizard(witch: Witch?) : AbstractWitch(witch) {
    fun tick() {
        val equipment: EntityEquipment = getBaseEntity().getEquipment()
        equipment.setItemInMainHand(ItemStack(Material.FIRE_CHARGE))
        SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.DRIP_LAVA, 5)
        if (canShoot(5)) {
            SpecialMobUtil.launchProjectileOnTarget(getBaseEntity(), LargeFireball::class.java, 4)
            shot()
        }
    }

    fun onProjectileShoot(event: ProjectileLaunchEvent) {
        if (event.getEntity().getType() == EntityType.FIREBALL) return
        event.setCancelled(true)
    }

    fun onProjectileHit(event: ProjectileHitEvent) {
        ObjUtil.nonNull<Projectile>(event.getEntity(), Consumer<Projectile> { e: Projectile ->
            if (e.getType() == EntityType.PLAYER) {
                val attribute: AttributeInstance = getBaseEntity().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)
                (event.getHitEntity() as LivingEntity).damage(attribute.getValue(), getBaseEntity())
            }
        })
    }

    init {
        tick()
    }
}