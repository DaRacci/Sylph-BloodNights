package me.racci.bloodnight.specialmobs.mobs.phantom

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractPhantom
import org.bukkit.attribute.Attribute

class FirePhantom(phantom: Phantom?) : AbstractPhantom(phantom) {
    private val blaze: Blaze
    override fun onEnd() {
        blaze.remove()
    }

    fun onDamageByEntity(event: EntityDamageByEntityEvent?) {}
    fun onDamage(event: EntityDamageEvent?) {
        SpecialMobUtil.handleExtendedEntityDamage(getBaseEntity(), blaze, event)
    }

    fun onExtensionDamage(event: EntityDamageEvent?) {
        SpecialMobUtil.handleExtendedEntityDamage(blaze, getBaseEntity(), event)
    }

    fun onTargetEvent(event: EntityTargetEvent) {
        blaze.setTarget(if (event.getTarget() == null) null else event.getTarget() as LivingEntity)
    }

    fun onDeath(event: EntityDeathEvent?) {
        blaze.damage(blaze.getHealth(), getBaseEntity())
    }

    init {
        blaze = SpecialMobUtil.spawnAndMount(getBaseEntity(), EntityType.BLAZE)
        AttributeUtil.syncAttributeValue(phantom, blaze, Attribute.GENERIC_ATTACK_DAMAGE)
        AttributeUtil.syncAttributeValue(phantom, blaze, Attribute.GENERIC_MAX_HEALTH)
    }
}