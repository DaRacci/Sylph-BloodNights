package me.racci.bloodnight.specialmobs

import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.*

abstract class SpecialMob<T : LivingEntity>(val baseEntity: T) {
    /**
     * Called at a fixed amount of ticks while the blood night is active.
     *
     *
     * Counting ticks is not a best practice, since the tick speed is not fixed and can be changed.
     *
     *
     * Use [Instant] to measure time since the last action.
     */
    abstract fun tick()

    /**
     * Called when a blood night ends and the special mob is going to be removed in the next step.
     *
     *
     * The mob will be removed by the [remove] method. Therefore, this method should not remove the mob.
     */
    abstract fun onEnd()

    /**
     * Called when the special mob teleports.
     *
     * @param event Event which was dispatched for this mob
     */
    abstract fun onTeleport(event: EntityTeleportEvent)

    /**
     * Called when the special mob launches a projectile.
     *
     * @param event Event which was dispatched for this mob
     */
    abstract fun onProjectileShoot(event: ProjectileLaunchEvent)

    /**
     * Called when a projectile launched by the special mob hit something.
     *
     * @param event Event which was dispatched for this mob
     */
    abstract fun onProjectileHit(event: ProjectileHitEvent)

    /**
     * Called when the special mob dies.
     *
     * @param event The death event of the death of the special mob.
     */
    abstract fun onDeath(event: EntityDeathEvent)

    /**
     * Called when the special mob kills another entity.
     *
     * @param event The death event of the killed entity.
     */
    abstract fun onKill(event: EntityDeathEvent)

    /**
     * Called when a special mob starts to explode.
     *
     * @param event event of the special mob starting to explode
     */
    abstract fun onExplosionPrimeEvent(event: ExplosionPrimeEvent)

    /**
     * Called when a special mob exploded.
     *
     * @param event event of the explosion of the special mob
     */
    abstract fun onExplosionEvent(event: EntityExplodeEvent)

    /**
     * Called when a special mob changes its target.
     *
     *
     * This will only be called, when the new target is of type player or null.
     *
     *
     * A special mob will never target something else then a player.
     *
     * @param event event containing the new target
     */
    abstract fun onTargetEvent(event: EntityTargetEvent)

    /**
     * Called when the special mob takes damage
     *
     *
     * This is a less specific version of [.onDamageByEntity]. Do not implement both.
     *
     * @param event damage event of the special mob taking damage
     */
    abstract fun onDamage(event: EntityDamageEvent)

    /**
     * Called when the entity takes damage from another entity
     *
     *
     * This is a more specific version of [.onDamage]. Do not implement both.
     *
     * @param event damage event of the special mob taking damage
     */
    abstract fun onDamageByEntity(event: EntityDamageByEntityEvent)

    /**
     * Called when the entity damages another entity
     *
     * @param event event of the special mob dealing damage
     */
    abstract fun onHit(event: EntityDamageByEntityEvent)

    /**
     * Attemts to remove the base entity.
     *
     *
     * This should not be overridden unless your entity has a extension.
     *
     *
     * If you override this just remove the extension and call super afterwards.
     */
    open fun remove() {
        if (baseEntity.isValid) {
            baseEntity.remove()
        }
    }

    /**
     * This event is called when a entity which is tagged as special mob extension receives damage. This will be most
     * likely the passenger or the carrier of a special mob.
     *
     *
     * This event should be used for damage synchronization.
     *
     *
     * Best practise should be that the damage to the extension is forwarded to the base mob.
     *
     *
     * Dont implement this if the special mob doesn't has an extension.
     *
     * @param event damage event of the extension taking damage,
     */
    abstract fun onExtensionDamage(event: EntityDamageEvent)

    /**
     * This event is called when a entity which is tagged as special mob extension is killed.
     *
     *
     * This will be most likely the passenger or the carrier of a special mob.
     *
     *
     * This event should be used to kill the remaining entity.
     *
     *
     * Dont implement this if the mob doesn't has an extension.
     *
     * @param event damage event of the extension taking damage,
     */
    abstract fun onExtensionDeath(event: EntityDeathEvent)

    /**
     * Checks if the entity is valid.
     *
     *
     * The entity is valid if the base entity is valid.
     *
     * @return true when the base entity is valid.
     */
    open val isValid: Boolean
        get() = baseEntity.isValid
}