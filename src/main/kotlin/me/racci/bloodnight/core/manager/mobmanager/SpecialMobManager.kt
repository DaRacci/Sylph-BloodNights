package me.racci.bloodnight.core.manager.mobmanager

import de.eldoria.eldoutilities.entityutils.ProjectileUtil
import de.eldoria.eldoutilities.threading.IteratingTask
import de.eldoria.eldoutilities.threading.TaskStatistics
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.core.api.BloodNightEndEvent
import me.racci.bloodnight.core.manager.nightmanager.NightManager
import me.racci.bloodnight.core.mobfactory.MobFactory
import me.racci.bloodnight.specialmobs.SpecialMob
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.events.HollowsEve2021
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class SpecialMobManager(nightManager: NightManager, configuration: Configuration) : BukkitRunnable(), Listener {

    private val mobRegistry = HashMap<String, WorldMobs>()
    val nightManager: NightManager
    val configuration: Configuration
    private val lostEntities = ArrayDeque<Entity>()

    fun wrapMob(entity: Entity, mobFactory: MobFactory) {
        if (entity !is LivingEntity) return
        if (SpecialMobUtil.isSpecialMob(entity)) return

        val mobSettings = configuration.getWorldSettings(entity.world.name).mobSettings
        val mobSetting = mobSettings.getMobByName(mobFactory.mobName) ?: return

        val specialMob = mobFactory.wrap(entity, mobSettings, mobSetting)
        (entity as? Ageable)?.setAdult()
        HollowsEve2021.dropChances(entity)
        entity.health = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
        registerMob(specialMob)
    }

    /*fun wrapHollowsEve(entity: Entity, mobFactory: MobFactory) {
        if (entity !is LivingEntity) return
        val mobSettings = configuration.getWorldSettings(entity.world.name).mobSettings
        val mobSetting = mobSettings.getMobByName(mobFactory.mobName) ?: return

        val mob = if(mobFactory.entityType != entity.type) {
            val loc = entity.location
            entity.remove()
            loc.world.spawn(loc, mobFactory.entityType.entityClass!!) {
                it.persistentDataContainer[MobManager.NO_TOUCH, PersistentDataType.BYTE] = 1.toByte()
            } as LivingEntity
        } else entity

        val sm = mobFactory.wrap(mob, mobSettings, mobSetting)
        (mob as? Ageable)?.setAdult()
        HollowsEve2021.dropChances(mob)
        mob.canPickupItems = false
        mob.health = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
        registerMob(sm)

    }*/

    override fun run() {
        for (bloodWorld in nightManager.bloodWorldsSet) {
            getWorldMobs(bloodWorld).tick(configuration.generalSettings.mobTick)
        }
        for (i in 0 until lostEntities.size.coerceAtMost(10)) {
            val poll = lostEntities.poll()
            if (poll.isValid) {
                poll.remove()
            }
        }
    }

    fun getWorldMobs(world: World): WorldMobs {
        return mobRegistry.computeIfAbsent(world.name) { WorldMobs() }
    }

    /*
    START OF EVENT REDIRECTING SECTION
    */
    @EventHandler
    fun onEntityTeleport(event: EntityTeleportEvent) {
        getWorldMobs(event.entity.world).invokeIfPresent(event.entity) { it.onTeleport(event) }
    }

    @EventHandler
    fun onProjectileShoot(event: ProjectileLaunchEvent) {
        val projectileSource = ProjectileUtil.getProjectileSource(event.entity)
        if (projectileSource.isEntity) {
            getWorldMobs(event.entity.world).invokeIfPresent(projectileSource.entity) { it.onProjectileShoot(event) }
        }
    }

    @EventHandler
    fun onProjectileHit(event: ProjectileHitEvent) {
        val projectileSource = ProjectileUtil.getProjectileSource(event.entity)
        if (projectileSource.isEntity) {
            getWorldMobs(event.entity.world).invokeIfPresent(projectileSource.entity) { it.onProjectileHit(event) }
        }
    }

    @EventHandler
    fun onDeath(event: EntityDeathEvent) {
        if (SpecialMobUtil.isSpecialMob(event.entity)) {
            if (SpecialMobUtil.isExtension(event.entity)) {
                val baseUUID = SpecialMobUtil.getBaseUUID(event.entity)
                getWorldMobs(event.entity.world).invokeIfPresent(baseUUID) { it.onExtensionDeath(event) }
            } else {
                getWorldMobs(event.entity.world).invokeIfPresent(event.entity) { it.onDeath(event) }
            }
        }
    }

    @EventHandler
    fun onKill(event: EntityDeathEvent) {
        getWorldMobs(event.entity.world).invokeIfPresent(event.entity) { it.onKill(event) }
    }

    @EventHandler
    fun onExplosionPrimeEvent(event: ExplosionPrimeEvent) {
        getWorldMobs(event.entity.world).invokeIfPresent(event.entity) { it.onExplosionPrimeEvent(event) }
    }

    @EventHandler
    fun onExplosionEvent(event: EntityExplodeEvent) {
        getWorldMobs(event.entity.world).invokeIfPresent(event.entity) { it.onExplosionEvent(event) }
    }

    @EventHandler
    fun onTargetEvent(event: EntityTargetEvent) {

        if (event.target != null
            && SpecialMobUtil.isSpecialMob(event.target!!)
        ) {
            event.isCancelled = true; return
        }

        if (!SpecialMobUtil.isSpecialMob(event.entity)) return

        if (event.target != null
            && event.target!!.type != EntityType.PLAYER
        ) {
            event.isCancelled = true; return
        }

        if (event.target == null || event.target is LivingEntity) {
            getWorldMobs(event.entity.world).invokeIfPresent(event.entity) { it.onTargetEvent(event) }
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (!SpecialMobUtil.isSpecialMob(event.entity)) return
        if (SpecialMobUtil.isExtension(event.entity)) {
            val baseUUID = SpecialMobUtil.getBaseUUID(event.entity)
            getWorldMobs(event.entity.world).invokeIfPresent(baseUUID) { it.onExtensionDamage(event) }
        } else {
            getWorldMobs(event.entity.world).invokeIfPresent(event.entity) { it.onDamage(event) }
        }
    }

    @EventHandler
    fun onDamageByEntity(event: EntityDamageByEntityEvent) {
        if (!SpecialMobUtil.isSpecialMob(event.entity)) return
        if (SpecialMobUtil.isExtension(event.entity)) {
            val baseUUID = SpecialMobUtil.getBaseUUID(event.entity)
            getWorldMobs(event.entity.world).invokeIfPresent(baseUUID) { it.onDamageByEntity(event) }
        } else {
            getWorldMobs(event.entity.world).invokeIfPresent(event.entity) { it.onDamageByEntity(event) }
        }
    }

    @EventHandler
    fun onHit(event: EntityDamageByEntityEvent) {
        if(!SpecialMobUtil.isSpecialMob(event.damager)) return
        getWorldMobs(event.entity.world).invokeIfPresent(event.entity) { it.onHit(event) }
    }

    /*
    END OF EVENT REDIRECTION SECTION
     */
    private fun registerMob(mob: SpecialMob<*>) {
        val world: World = mob.baseEntity.location.world
        getWorldMobs(world).put(mob.baseEntity.uniqueId, mob)
    }

    @EventHandler
    fun onBloodNightEnd(event: BloodNightEndEvent) {
        val worldMobs = getWorldMobs(event.world)
        worldMobs.invokeAll(SpecialMob<*>::onEnd)
        worldMobs.invokeAll(SpecialMob<*>::remove)
        worldMobs.clear()
        val iteratingTask: IteratingTask<Entity> =
            IteratingTask(event.world.entities, result@{
                if (it !is LivingEntity) return@result false
                if (SpecialMobUtil.isSpecialMob(it)) {
                    lostEntities.add(it); return@result true
                }
                false
            }, { stats: TaskStatistics ->
                BloodNight.logger()
                    .config("Marked ${stats.processedElements} lost entities for removal in ${stats.time}ms")
            })
        iteratingTask.runTaskTimer(BloodNight.instance, 5, 1)
    }

    fun remove(entity: Entity) {
        getWorldMobs(entity.world).remove(entity.uniqueId)
    }

    init {
        this.nightManager = nightManager
        this.configuration = configuration
    }
}