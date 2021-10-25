package me.racci.bloodnight.specialmobs.mobs.zombie

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.entity.Zombie
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractZombie

class ArmoredZombie(zombie: Zombie) : AbstractZombie(zombie) {
    init {
        val equipment = zombie.equipment
        equipment.helmet = ItemStack(Material.DIAMOND_HELMET)
        equipment.chestplate = ItemStack(Material.DIAMOND_CHESTPLATE)
        equipment.leggings = ItemStack(Material.DIAMOND_LEGGINGS)
        equipment.boots = ItemStack(Material.DIAMOND_BOOTS)
    }
}