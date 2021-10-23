package me.racci.bloodnight.specialmobs

import de.eldoria.eldoutilities.serialization.TypeConversion
import de.eldoria.eldoutilities.utils.EMath
import de.eldoria.eldoutilities.utils.ERandom
import me.racci.bloodnight.config.worldsettings.deathactions.subsettings.LightningSettings
import me.racci.bloodnight.config.worldsettings.deathactions.subsettings.ShockwaveSettings
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.specialmobs.effects.ParticleCloud
import me.racci.bloodnight.specialmobs.effects.PotionCloud
import me.racci.bloodnight.util.VectorUtil
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.NumberConversions
import org.bukkit.util.Vector
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer
import java.util.logging.Level
import kotlin.math.abs

object SpecialMobUtil {
    private val IS_SPECIAL_MOB: NamespacedKey = BloodNight.getNamespacedKey("isSpecialMob")
    private val IS_MOB_EXTENSION: NamespacedKey = BloodNight.getNamespacedKey("isMobExtension")
    private val BASE_UUID: NamespacedKey = BloodNight.getNamespacedKey("baseUUID")
    private val MOB_TYPE: NamespacedKey = BloodNight.getNamespacedKey("mobType")

    @Deprecated("")
    fun spawnLingeringPotionAt(location: Location, potionEffect: PotionEffect) {
        spawnPotionAt(location, potionEffect, Material.LINGERING_POTION)
    }

    @Deprecated("")
    fun spawnPotionAt(location: Location, potionEffect: PotionEffect, potionType: Material) {
        val potion = ItemStack(potionType)
        val potionMeta: PotionMeta = potion.itemMeta as PotionMeta
        potionMeta.addCustomEffect(potionEffect, false)
        potion.setItemMeta(potionMeta)
        val entity: LivingEntity =
            location.world.spawnEntity(location.add(0.0, 1.0, 0.0), EntityType.CHICKEN) as LivingEntity
        val thrownPotion: ThrownPotion =
            entity.launchProjectile(ThrownPotion::class.java, Vector(0, -4, 0))
        thrownPotion.item = potion
        entity.remove()
    }

    /**
     * Adds a simple potion effect to an entity.
     *
     * @param entity    entity to add potion effect
     * @param type      type of potion effect
     * @param amplifier amplifier
     * @param visible   true if particles should be visible
     */
    fun addPotionEffect(entity: LivingEntity, type: PotionEffectType, amplifier: Int, visible: Boolean) {
        entity.addPotionEffect(PotionEffect(type, 60 * 20, amplifier, visible, visible))
    }

    /**
     * Spawns particle around a entity.
     *
     * @param entity   entity as center
     * @param particle particle to spawn
     * @param amount   amount to spawn
     */
    fun spawnParticlesAround(entity: Entity, particle: Particle, amount: Int) {
        spawnParticlesAround(entity.location, particle, amount)
    }

    /**
     * Spawns particle around a location.
     *
     * @param location location as center
     * @param particle particle to spawn
     * @param amount   amount to spawn
     */
    fun spawnParticlesAround(location: Location, particle: Particle, amount: Int) {
        spawnParticlesAround<Any>(location, particle, null, amount)
    }

    /**
     * Spawns particle around a location.
     *
     * @param location location as center
     * @param particle particle to spawn
     * @param data     data which may be required for spawning the particle
     * @param amount   amount to spawn
     * @param <T>      type of date
    </T> */
    fun <T> spawnParticlesAround(location: Location, particle: Particle, data: T?, amount: Int) {
        val world: World = location.world
        val rand: ThreadLocalRandom = ThreadLocalRandom.current()
        for (i in 0 until amount) {
            if (data != null) {
                world.spawnParticle<T>(
                    particle,
                    location.clone()
                        .add(
                            rand.nextDouble(-3.0, 3.0),
                            rand.nextDouble(0.0, 3.0),
                            rand.nextDouble(-3.0, 3.0)
                        ),
                    1, data
                )
            } else {
                world.spawnParticle(
                    particle,
                    location.clone()
                        .add(
                            rand.nextDouble(-3.0, 3.0),
                            rand.nextDouble(0.0, 3.0),
                            rand.nextDouble(-3.0, 3.0)
                        ),
                    1
                )
            }
        }
    }

    /**
     * Launches a projectile on the current target of the entity.
     *
     * @param source     source of the projectile.
     * @param projectile projectile type
     * @param speed      projectile speed
     * @param <T>        type of projectile
     * @return projectile or null if target is null
    </T> */
    fun <T : Projectile> launchProjectileOnTarget(source: Mob, projectile: Class<T>, speed: Double): T? {
        return launchProjectileOnTarget(source, source.target, projectile, speed)
    }

    /**
     * Launches a projectile.
     *
     * @param source     source of the projectile.
     * @param target     target of the projectile.
     * @param projectile projectile type
     * @param speed      projectile speed
     * @param <T>        type of projectile
     * @return projectile or null if target is null
    </T> */
    fun <T : Projectile> launchProjectileOnTarget(
        source: Mob,
        target: Entity?,
        projectile: Class<T>,
        speed: Double
    ): T? {
        if (target != null) {
            val vel: Vector = VectorUtil.getDirectionVector(source.location, target.location)
                .normalize()
                .multiply(speed)
            return source.launchProjectile(projectile, vel)
        }
        return null
    }

    /**
     * Spawn and mount a entity as a passenger
     *
     * @param passengerType type of passenger
     * @param carrier       carrier where the passenger should be mounted on
     * @param <T>           type of carrier
     * @return spawned passenger which is already mounted.
    </T> */
    fun <T : Entity> spawnAndMount(carrier: Entity, passengerType: EntityType): T {
        val passenger = spawnAndTagEntity<T>(carrier.location, passengerType)
        tagExtension(passenger, carrier)
        carrier.addPassenger(passenger)
        return passenger
    }

    /**
     * Spawn and mount a entity on a carrier
     *
     * @param carrierType type of carrier
     * @param rider       rider to mount
     * @param <T>         type of carrier
     * @return spawned carrier with the rider mounted.
    </T> */
    fun <T : Entity> spawnAndMount(carrierType: EntityType, rider: Entity): T {
        val carrier = spawnAndTagEntity<T>(rider.location, carrierType)
        tagExtension(carrier, rider)
        carrier.addPassenger(rider)
        return carrier
    }

    /**
     * Spawns a new entity and tags it as special mob.
     *
     * @param location   location of the new entity
     * @param entityType type of the entity
     * @param <T>        type of the entity
     * @return spawned entity of type
    </T> */
    fun <T : Entity> spawnAndTagEntity(location: Location, entityType: EntityType): T {
        val entity = location.world.spawnEntity(location, entityType)
        tagSpecialMob(entity)
        return entity as T
    }

    /**
     * Marks a entity as a special mob extension
     *
     * @param entity   entity to mark
     * @param extended the entity which is extended
     */
    fun tagExtension(entity: Entity, extended: Entity) {
        val dataContainer: PersistentDataContainer = entity.persistentDataContainer
        dataContainer.set(IS_MOB_EXTENSION, PersistentDataType.BYTE, 1.toByte())
        dataContainer.set(
            BASE_UUID, PersistentDataType.BYTE_ARRAY,
            TypeConversion.getBytesFromUUID(extended.uniqueId)
        )
    }

    /**
     * Checks if a entity is a extension of a special mob.
     *
     * @param entity entity to check
     * @return true if the entity is a extension
     */
    fun isExtension(entity: Entity) =
        entity.persistentDataContainer.has(IS_MOB_EXTENSION, PersistentDataType.BYTE)

    /**
     * Get the UUID of the base mob. This will only return a UUID if [.isExtension] returns true
     *
     * @param entity entity to check
     * @return returns a uuid if the mob is a extension.
     */
    fun getBaseUUID(entity: Entity) =
        TypeConversion.getUUIDFromBytes(
            entity.persistentDataContainer.get(BASE_UUID, PersistentDataType.BYTE_ARRAY))

    // Old method maybe needed
//    : UUID? {
//        val pdc = entity.persistentDataContainer
//        return if (pdc.has(IS_MOB_EXTENSION, PersistentDataType.BYTE)) {
//            pdc.get(BASE_UUID, PersistentDataType.BYTE_ARRAY)
//            val specialMob: ByteArray =
//                pdc.get(BASE_UUID, PersistentDataType.BYTE_ARRAY)
//            return TypeConversion.getUUIDFromBytes(specialMob)
//        } else null
//    }

    /**
     * Set the special mob type.
     *
     * @param entity entity to set
     * @param type   type to set
     */
    fun setSpecialMobType(entity: Entity, type: String) {
        entity.persistentDataContainer.set(MOB_TYPE, PersistentDataType.STRING, type)
    }

    /**
     * Get the Special Mob type
     *
     * @param entity entity to check
     * @return optional string with the mob type or null
     */
    fun getSpecialMobType(entity: Entity) =
        entity.persistentDataContainer.get(MOB_TYPE, PersistentDataType.STRING)

    // Old Method
//        val dataContainer: PersistentDataContainer = entity.persistentDataContainer
//        return if (dataContainer.has(MOB_TYPE, PersistentDataType.STRING)) {
//            Optional.ofNullable(
//                dataContainer.get(
//                    MOB_TYPE,
//                    PersistentDataType.STRING
//                )
//            )
//        } else null
//    }

    /**
     * Tags an entity as special mob.
     *
     * This also sets [Entity.setPersistent] to `false`
     * and [LivingEntity.setRemoveWhenFarAway] to `true`
     *
     * @param entity entity to tag
     */
    fun tagSpecialMob(entity: Entity) {
        val dataContainer: PersistentDataContainer = entity.persistentDataContainer
        dataContainer.set(IS_SPECIAL_MOB, PersistentDataType.BYTE, 1.toByte())
        if (entity is LivingEntity) entity.removeWhenFarAway = true
        entity.isPersistent = false
    }

    /**
     * Checks if a mob is a special mob
     *
     * @param entity entity to check
     * @return true if the mob is a special mob
     */
    fun isSpecialMob(entity: Entity) =
        entity.persistentDataContainer.has(IS_SPECIAL_MOB, PersistentDataType.BYTE)

    // Old Method
//        val dataContainer: PersistentDataContainer = entity.persistentDataContainer
//        if (dataContainer.has(IS_SPECIAL_MOB, PersistentDataType.BYTE)) {
//            val specialMob: Byte = dataContainer.get<Byte, Byte>(IS_SPECIAL_MOB, PersistentDataType.BYTE)
//            return specialMob != null && specialMob == 1.toByte()
//        }
//        return false
//    }

    /**
     * Handles the damage which was dealt to one entity to the extension or base.
     *
     * @param receiver receiver of the damage.
     * @param other    the other part of the mob.
     * @param event    the damage event
     */
    fun handleExtendedEntityDamage(receiver: LivingEntity, other: LivingEntity, event: EntityDamageEvent) {
        if(event is EntityDamageByEntityEvent
            && event.damager.uniqueId === other.uniqueId) return

        if (receiver.health == 0.0) return
        val newHealth = 0.0.coerceAtLeast(other.health - event.finalDamage)
        if (newHealth == 0.0) {
            other.damage(event.finalDamage, event.entity)
            return
        }
        other.health = newHealth
        other.playEffect(EntityEffect.HURT)
    }

    /**
     * Dispatch a shockwave at a location when probability is given.
     *
     * @param settings shockwave settings
     * @param location location of shockwave
     */
    fun dispatchShockwave(settings: ShockwaveSettings, location: Location) {
        if (settings.getShockwaveProbability() < ThreadLocalRandom.current().nextInt(101)
            || settings.getShockwaveProbability() === 0
        ) return
        val randomVector: Collection<Vector> = ERandom.getRandomVector(100)
        val world: World = location.world
        for (vector in randomVector) {
            vector.multiply(settings.getShockwaveRange())
            world.spawnParticle(
                Particle.EXPLOSION_NORMAL, location, 0, vector.x, vector.y, vector.z,
                settings.getShockwaveRange() / 100.0
            )
        }
        for (entity in getEntitiesAround(location, settings.getShockwaveRange())) {
            if (entity.location == location) continue
            val directionVector: Vector = VectorUtil.getDirectionVector(location, entity.location)
            val power: Double = settings.getPower(directionVector)
            val normalize = directionVector.clone().normalize()
            if (abs(normalize.y) <= 0.1) {
                BloodNight.logger().log(Level.FINE, "Adjusting shockwave direction. Vector is too flat.")
                normalize.add(Vector(0.0, 0.1, 0.0))
            }
            normalize.multiply(power)
            if (!NumberConversions.isFinite(normalize.x) || !NumberConversions.isFinite(normalize.z)) {
                continue
            }
            normalize.y = EMath.clamp(-0.2, 0.2, normalize.y)
            BloodNight.logger().log(
                Level.FINE,
                "Launching entity in direction $normalize | Power: $power | Initial direction: $directionVector"
            )
            entity.velocity = normalize
            settings.applyEffects(entity, power)
        }
    }

    fun dispatchLightning(settings: LightningSettings, location: Location) {
        if (location.world == null) return
        if (settings.isDoLightning() && settings.getLightning() !== 0) {
            if (ThreadLocalRandom.current().nextInt(101) <= settings.getLightning()) {
                location.world.strikeLightningEffect(location.clone().add(0.0, 10.0, 0.0))
                return
            }
        }
        if (settings.isDoThunder() && settings.getThunder() !== 0) {
            if (ThreadLocalRandom.current().nextInt(101) <= settings.getThunder()) {
                location.world.players.forEach(Consumer { p: Player ->
                    p.playSound(
                        p.location,
                        Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                        SoundCategory.WEATHER,
                        1f,
                        1f
                    )
                })
            }
        }
    }

    fun getEntitiesAround(location: Location, range: Double): Collection<Entity> {
        return if (location.world == null) emptyList() else location.world.getNearbyEntities(
            location,
            range,
            range,
            range
        )
    }

    /**
     * Builds a particle cloud and binds it to an entity.
     *
     *
     * In the most cases [.spawnParticlesAround] will result in a better result.
     *
     * @param target target to which the particle cloud should be bound.
     * @return builder
     */
    fun buildParticleCloud(target: LivingEntity) =
        ParticleCloud.builder(target)

    /**
     * Build a particle cloud which is bound to an entity
     *
     * @param target entity which will be followed by the cloud
     * @return builder
     */
    fun buildPotionCloud(target: LivingEntity) =
        PotionCloud.builder(target)

    /**
     * Build a particle cloud at a specific location.
     *
     * @param location location where the cloud should be created
     * @return builder
     */
    fun buildParticleCloud(location: Location) =
        PotionCloud.builder(location)
}