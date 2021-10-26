package me.racci.bloodnight.specialmobs.mobs.zombie

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractZombie
import org.bukkit.entity.Zombie
import org.bukkit.potion.PotionEffectType

class SpeedZombie(zombie: Zombie) : AbstractZombie(zombie) {

    override fun tick() {
        SpecialMobUtil.addPotionEffect(baseEntity, PotionEffectType.SPEED, 4, true)
    }
}