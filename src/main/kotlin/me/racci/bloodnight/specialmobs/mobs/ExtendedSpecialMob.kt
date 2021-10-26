@file:Suppress("KDocUnresolvedReference")

package me.racci.bloodnight.specialmobs.mobs

import de.eldoria.eldoutilities.utils.AttributeUtil
import me.racci.bloodnight.specialmobs.SpecialMob
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.StatSource
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityTargetEvent

/**
 * Create a new special mob from a carrier and passenger entity.
 *
 *
 * [ExtendedSpecialMob.carrier] and [ExtendedSpecialMob.passenger] take precedence.
 *
 * @param carrier    carrier
 * @param passenger  passenger
 * @param statSource defines which of the both entity should provide the stats for the other entity
 */
abstract class ExtendedSpecialMob<T : Mob, U : Mob>(val carrier: T, val passenger: U, statSource: StatSource) :
    SpecialMob<T>(carrier) {

    /**
     * Create a new extended special mob from a carrier with a passenger.
     *
     * @param carrier   carrier to bind
     * @param passenger to spawn
     */
    constructor(carrier: T, passenger: EntityType) : this(
        carrier,
        SpecialMobUtil.spawnAndMount(carrier, passenger),
        StatSource.CARRIER
    )

    /**
     * Create a new extended special mob from passenger with a carrier.
     *
     * @param carrier   carrier to spawn
     * @param passenger passenger to bind
     */
    constructor(carrier: EntityType, passenger: U) : this(
        SpecialMobUtil.spawnAndMount(carrier, passenger),
        passenger,
        StatSource.PASSENGER
    )

    /**
     * This method already kills the extension.
     *
     *
     * {@inheritDoc}
     *
     * @param event The death event of the death of the special mob.
     */
    override fun onDeath(event: EntityDeathEvent) {
        baseEntity.damage(passenger.health, baseEntity)
    }

    /**
     * This method already synchronises the target between carrier and extension.
     *
     *
     * {@inheritDoc}
     *
     * @param event event containing the new target
     */
    override fun onTargetEvent(event: EntityTargetEvent) {
        if (event.target == null) {
            passenger.target = null; return
        }
        if (event.target is LivingEntity) {
            passenger.target = event.target as LivingEntity
        }
    }

    /**
     * This method already forwards damage to the extension.
     *
     *
     * {@inheritDoc}
     *
     * @param event damage event of the special mob taking damage
     */
    override fun onDamage(event: EntityDamageEvent) {
        SpecialMobUtil.handleExtendedEntityDamage(baseEntity, passenger, event)
    }

    /**
     * This method already forwards damage to the carrier.
     *
     *
     * {@inheritDoc}
     *
     * @param event damage event of the extension taking damage,
     */
    override fun onExtensionDamage(event: EntityDamageEvent) {
        SpecialMobUtil.handleExtendedEntityDamage(passenger, baseEntity, event)
    }

    /**
     * This method already forwards damage to the carrier.
     *
     *
     * {@inheritDoc}
     *
     * @param event damage event of the extension taking damage,
     */
    override fun onExtensionDeath(event: EntityDeathEvent) {
        baseEntity.damage(baseEntity.health, event.entity.killer)
    }

    /**
     * This method already removes the extension.
     *
     *
     * {@inheritDoc}
     */
    override fun remove() {
        passenger.remove()
        super.remove()
    }

    /**
     * {@inheritDoc} The extended special mob is only valid when the base entity has a passenger and the passenger is
     * valid as well.
     *
     * @return true when the base entity and passenger is valid and the base entity has a passenger.
     */
    override val isValid: Boolean
        get() = super.isValid && passenger.isValid && baseEntity.passengers.isNotEmpty()

    init {
        val source: Mob = if (statSource === StatSource.PASSENGER) passenger else carrier
        val target: Mob = if (statSource === StatSource.PASSENGER) carrier else passenger
        target.customName = source.customName
        target.isCustomNameVisible = source.isCustomNameVisible
        AttributeUtil.syncAttributeValue(source, target, Attribute.GENERIC_ATTACK_DAMAGE)
        AttributeUtil.syncAttributeValue(source, target, Attribute.GENERIC_MAX_HEALTH)
    }
}