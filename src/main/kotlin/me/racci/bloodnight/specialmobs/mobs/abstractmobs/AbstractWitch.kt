package me.racci.bloodnight.specialmobs.mobs.abstractmobs

import me.racci.bloodnight.specialmobs.SpecialMob
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import org.bukkit.Particle
import org.bukkit.entity.Witch
import java.time.Instant
import java.time.temporal.ChronoUnit

abstract class AbstractWitch(witch: Witch) : SpecialMob<Witch>(witch) {

    private var lastShot: Instant = Instant.now()

    override fun onEnd() {
        SpecialMobUtil.spawnParticlesAround(baseEntity, Particle.CAMPFIRE_COSY_SMOKE, 30)
    }

    protected fun shot() {
        lastShot = Instant.now()
    }

    /**
     * check if last shot is in the past more than the delay
     *
     * @param delay delay in seconds
     * @return true if entity can shoot again
     */
    protected fun canShoot(delay: Int): Boolean {
        return lastShot.isBefore(Instant.now().minus(delay.toLong(), ChronoUnit.SECONDS))
    }
}