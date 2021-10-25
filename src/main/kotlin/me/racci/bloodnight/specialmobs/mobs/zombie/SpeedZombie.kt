package me.racci.bloodnight.specialmobs.mobs.zombie

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractZombie

class SpeedZombie(zombie: Zombie?) : AbstractZombie(zombie) {
    fun tick() {
        SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.SPEED, 4, true)
    }
}