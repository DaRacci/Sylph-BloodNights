package me.racci.bloodnight.specialmobs.mobs.phantom

import de.eldoria.eldoutilities.utils.AttributeUtil
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractPhantom
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Blaze
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Phantom
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityTargetEvent

class FirePhantom(phantom: Phantom) : AbstractPhantom(phantom) {

    private val blaze: Blaze = SpecialMobUtil.spawnAndMount(baseEntity, EntityType.BLAZE)

    override fun onEnd() {
        blaze.remove()
    }

    override fun onDamage(event: EntityDamageEvent) {
        SpecialMobUtil.handleExtendedEntityDamage(baseEntity, blaze, event)
    }

    override fun onExtensionDamage(event: EntityDamageEvent) {
        SpecialMobUtil.handleExtendedEntityDamage(blaze, baseEntity, event)
    }

    override fun onTargetEvent(event: EntityTargetEvent) {
        blaze.target = if (event.target == null) null else event.target as LivingEntity
    }

    override fun onDeath(event: EntityDeathEvent) {
        blaze.damage(blaze.health, baseEntity)
    }

    init {
        AttributeUtil.syncAttributeValue(phantom, blaze, Attribute.GENERIC_ATTACK_DAMAGE)
        AttributeUtil.syncAttributeValue(phantom, blaze, Attribute.GENERIC_MAX_HEALTH)
    }
}