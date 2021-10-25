package me.racci.bloodnight.specialmobs.mobs.zombie

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractZombie
import java.time.Instant

class InvisibleZombie(zombie: Zombie) : AbstractZombie(zombie) {
    private var lastDamage = Instant.now()
    fun tick() {
        if (lastDamage.isBefore(Instant.now().minusSeconds(10))) {
            SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.INVISIBILITY, 1, true)
        }
    }

    fun onDamageByEntity(event: EntityDamageByEntityEvent?) {
        lastDamage = Instant.now()
        getBaseEntity().removePotionEffect(PotionEffectType.INVISIBILITY)
    }

    init {
        val equipment: EntityEquipment = zombie.getEquipment()
        equipment.setItemInMainHand(ItemStack(Material.AIR))
        equipment.setHelmet(ItemStack(Material.AIR))
        equipment.setChestplate(ItemStack(Material.AIR))
        equipment.setLeggings(ItemStack(Material.AIR))
        equipment.setBoots(ItemStack(Material.AIR))
        tick()
    }
}