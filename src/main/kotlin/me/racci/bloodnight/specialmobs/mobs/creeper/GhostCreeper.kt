package me.racci.bloodnight.specialmobs.mobs.creeper

import de.eldoria.bloodnight.core.BloodNight
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
    fun tick() {
        if (legacy) {
            SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.INVISIBILITY, 4, false)
        }
        SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.SPEED, 4, false)
        super.tick()
    }

    fun onDamage(event: EntityDamageEvent) {
        if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
            event.setCancelled(true)
            return
        }
        super.onDamage(event)
    }

    fun onExtensionDamage(event: EntityDamageEvent) {
        if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
            event.setCancelled(true)
            return
        }
        super.onExtensionDamage(event)
    }

    fun onExplosionEvent(event: EntityExplodeEvent?) {
        getBaseEntity().remove()
    }

    init {
        val optVersion: Optional<Triple<Int, Int, Int>> = ServerVersion.extractVersion()
        // Entites can be invisible since 1.16.3. Hotfix for backwards compatibiliy to spigot 1.16.2
        if (optVersion.isPresent) {
            val version = optVersion.get()
            if (version.second >= 16 && version.third > 2) {
                getBaseEntity().setInvisible(true)
            } else {
                legacy = true
            }
        } else {
            legacy = true
        }
        getBaseEntity().setInvulnerable(true)
        object : BukkitRunnable() {
            override fun run() {
                getBaseEntity().getEquipment().setItemInMainHand(null)
                getBaseEntity().getEquipment().setItemInOffHand(null)
            }
        }.runTaskLater(BloodNight.getInstance(), 2)
        //AttributeUtil.setAttributeValue(getBaseEntity(), Attribute.GENERIC_FLYING_SPEED, 100);
    }
}