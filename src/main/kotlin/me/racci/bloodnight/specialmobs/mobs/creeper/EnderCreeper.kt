package me.racci.bloodnight.specialmobs.mobs.creeper

import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractCreeper
import org.bukkit.Color
import org.bukkit.EntityEffect
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Creeper
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.util.Vector
import java.time.Instant
import java.util.concurrent.ThreadLocalRandom

class EnderCreeper(creeper: Creeper) : AbstractCreeper(creeper) {

    private val rand: ThreadLocalRandom = ThreadLocalRandom.current()
    private var lastTeleport = Instant.now()

    override fun tick() {
        SpecialMobUtil.spawnParticlesAround(
            baseEntity.location, Particle.REDSTONE,
            Particle.DustOptions(Color.PURPLE, 2F), 5
        )
        teleportToTarget()
    }

    override fun onTargetEvent(event: EntityTargetEvent) {
        teleportToTarget()
    }

    override fun onDamageByEntity(event: EntityDamageByEntityEvent) {
        if (baseEntity.target === event.damager) {
            return
        }
        if (event.damager is LivingEntity) {
            baseEntity.target = event.damager as LivingEntity
        }
        teleportToTarget()
    }

    private fun teleportToTarget() {
        if (lastTeleport.isBefore(Instant.now().minusSeconds(5))) return
        val target = baseEntity.target ?: return
        val distance = target.location.distance(baseEntity.location)
        if (distance > 5) {
            val loc = target.location
            val fuzz = Vector(rand.nextDouble(-2.0, 2.0), 0.0, rand.nextDouble(-2.0, 2.0))
            val first = loc.world.getBlockAt(loc.add(fuzz))
            val second = first.getRelative(0, 1, 0)
            if (first.type == Material.AIR && second.type == Material.AIR) {
                val newLoc = first.location
                BloodNight.logger().finer("Teleport from " + baseEntity.location + " to " + newLoc)
                baseEntity.teleport(newLoc)
                lastTeleport = Instant.now()
                SpecialMobUtil.spawnParticlesAround(loc, Particle.PORTAL, 10)
                baseEntity.playEffect(EntityEffect.ENTITY_POOF)
            }
            if (lastTeleport.isBefore(Instant.now().minusSeconds(8))) {
                baseEntity.teleport(target.location)
                lastTeleport = Instant.now()
                SpecialMobUtil.spawnParticlesAround(loc, Particle.PORTAL, 10)
                baseEntity.playEffect(EntityEffect.ENTITY_POOF)
            }
        }
    }
}