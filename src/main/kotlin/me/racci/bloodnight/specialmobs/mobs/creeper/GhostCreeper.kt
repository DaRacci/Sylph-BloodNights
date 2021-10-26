package me.racci.bloodnight.specialmobs.mobs.creeper

import de.eldoria.eldoutilities.container.Triple
import de.eldoria.eldoutilities.crossversion.ServerVersion
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.ExtendedSpecialMob
import org.bukkit.entity.Creeper
import org.bukkit.entity.EntityType
import org.bukkit.entity.Vex
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class GhostCreeper(creeper: Creeper) : ExtendedSpecialMob<Vex, Creeper>(EntityType.VEX, creeper) {

    private var legacy = false

    override fun tick() {
        if (legacy) {
            SpecialMobUtil.addPotionEffect(baseEntity, PotionEffectType.INVISIBILITY, 4, false)
        }
        SpecialMobUtil.addPotionEffect(baseEntity, PotionEffectType.SPEED, 4, false)
        super.tick()
    }

    override fun onDamage(event: EntityDamageEvent) {
        if (event.cause == EntityDamageEvent.DamageCause.SUFFOCATION) {
            event.isCancelled = true
            return
        }
        super.onDamage(event)
    }

    override fun onExtensionDamage(event: EntityDamageEvent) {
        if (event.cause == EntityDamageEvent.DamageCause.SUFFOCATION) {
            event.isCancelled = true
            return
        }
        super.onExtensionDamage(event)
    }

    override fun onExplosionEvent(event: EntityExplodeEvent) {
        baseEntity.remove()
    }

    init {
        val optVersion: Optional<Triple<Int, Int, Int>> = ServerVersion.extractVersion()
        // Entities can be invisible since 1.16.3. Hotfix for backwards compatibility to spigot 1.16.2
        if (optVersion.isPresent) {
            val version = optVersion.get()
            if (version.second >= 16 && version.third > 2) {
                baseEntity.isInvisible = true
            } else {
                legacy = true
            }
        } else {
            legacy = true
        }
        baseEntity.isInvulnerable = true
        object : BukkitRunnable() {
            override fun run() {
                baseEntity.equipment.setItemInMainHand(null)
                baseEntity.equipment.setItemInOffHand(null)
            }
        }.runTaskLater(BloodNight.instance, 2)
        //AttributeUtil.setAttributeValue(getBaseEntity(), Attribute.GENERIC_FLYING_SPEED, 100);
    }
}