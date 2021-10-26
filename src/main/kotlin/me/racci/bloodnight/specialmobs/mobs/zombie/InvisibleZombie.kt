package me.racci.bloodnight.specialmobs.mobs.zombie

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractZombie
import org.bukkit.Material
import org.bukkit.entity.Zombie
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import java.time.Instant

class InvisibleZombie(zombie: Zombie) : AbstractZombie(zombie) {

    private var lastDamage = Instant.now()

    override fun tick() {
        if (lastDamage.isBefore(Instant.now().minusSeconds(10))) {
            SpecialMobUtil.addPotionEffect(baseEntity, PotionEffectType.INVISIBILITY, 1, true)
        }
    }

    override fun onDamageByEntity(event: EntityDamageByEntityEvent) {
        lastDamage = Instant.now()
        baseEntity.removePotionEffect(PotionEffectType.INVISIBILITY)
    }

    init {
        val equipment = zombie.equipment
        equipment.setItemInMainHand(ItemStack(Material.AIR))
        equipment.helmet = ItemStack(Material.AIR)
        equipment.chestplate = ItemStack(Material.AIR)
        equipment.leggings = ItemStack(Material.AIR)
        equipment.boots = ItemStack(Material.AIR)
        tick()
    }
}