package me.racci.bloodnight.core.manager.mobmanager

import de.eldoria.eldoutilities.entityutils.ProjectileSender
import de.eldoria.eldoutilities.entityutils.ProjectileUtil
import de.eldoria.eldoutilities.scheduling.DelayedActions
import de.eldoria.eldoutilities.threading.IteratingTask
import de.eldoria.eldoutilities.utils.DataContainerUtil
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.config.worldsettings.WorldSettings
import me.racci.bloodnight.config.worldsettings.mobsettings.MobSettings
import me.racci.bloodnight.config.worldsettings.mobsettings.VanillaMobSettings
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.core.BloodNight.Companion.configuration
import me.racci.bloodnight.core.BloodNight.Companion.nightManager
import me.racci.bloodnight.core.manager.nightmanager.NightManager
import me.racci.bloodnight.core.mobfactory.WorldMobFactory
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.Beehive
import org.bukkit.entity.Boss
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Flying
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicInteger

class MobManager(val nightManager: NightManager, val configuration: Configuration) : Listener {

    private val random: ThreadLocalRandom = ThreadLocalRandom.current()

    companion object {

        var specialMobManager: SpecialMobManager = SpecialMobManager(nightManager, configuration)
        var delayedActions: DelayedActions = DelayedActions.start(BloodNight.instance)
        val worldFucktories = HashMap<String, WorldMobFactory>()

        val NO_TOUCH    : NamespacedKey     = BloodNight.namespacedKey("notouch")
        val NO_DROP     : NamespacedKey     = BloodNight.namespacedKey("nodrop")
        val PICKED_UP   : NamespacedKey     = BloodNight.namespacedKey("pickedUp")
    }

    init {
        specialMobManager.runTaskTimer(BloodNight.instance, 100, 1)
        BloodNight.instance.registerListener(specialMobManager)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onHollowsEveMobSpawn(event: CreatureSpawnEvent) {
        if (!nightManager.isBloodNightActive(event.entity.world)) return
        if (!(event.entity is Monster || event.entity is Flying)) return
        if (event.spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            event.entity.persistentDataContainer[NO_DROP, PersistentDataType.BYTE] = 1.toByte(); return
        }
        if (event.location.y < 60) {event.isCancelled = true ; return}
        if (event.entity.persistentDataContainer.has(NO_TOUCH, PersistentDataType.BYTE)) return

        val world = event.location.world
        val mobSettings = configuration.getWorldSettings(world.name).mobSettings
        val mobFactory = getWorldMobFactory(world).getHollowsEveFactory(event.entity)
        if(mobFactory == null) {event.isCancelled = true ; return}

        if (75 < random.nextInt(101)) {event.isCancelled = true ; return}

        mobSettings.getMobByName(mobFactory.mobName)
        delayedActions.schedule({ specialMobManager.wrapHollowsEve(event.entity, mobFactory) }, 1)
    }


//    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//    fun onMobSpawn(event: CreatureSpawnEvent) {
//        if (!nightManager.isBloodNightActive(event.entity.world)) return
//        if (event.spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
//            event.entity.persistentDataContainer[NO_TOUCH, PersistentDataType.BYTE] = 1.toByte(); return
//        }
//        if (event.entity.persistentDataContainer.has(NO_TOUCH, PersistentDataType.BYTE)) return
//
//        val world = event.location.world
//        val mobSettings = configuration.getWorldSettings(world.name).mobSettings
//        val mobFactory = getWorldMobFactory(world).getRandomFactory(event.entity) ?: return
//
//        if (mobSettings.spawnPercentage < random.nextInt(101)) return
//
//        mobSettings.getMobByName(mobFactory.mobName)
//        delayedActions.schedule({ specialMobManager.wrapMob(event.entity, mobFactory) }, 1)
//    }

    private fun getWorldMobFactory(world: World) =
        worldFucktories.computeIfAbsent(world.name) { WorldMobFactory(configuration.getWorldSettings(it)) }

    /*
     * Handling of vanilla mobs damaging to players during a blood night
     */
    @EventHandler
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        if (!nightManager.isBloodNightActive(event.damager.world)) return

        val sender: ProjectileSender = ProjectileUtil.getProjectileSource(event.damager)
        val damager: Entity = if (sender.isEntity) sender.entity else event.damager
        val oponent: Entity = event.entity as? Player ?: return
        val settings: MobSettings = configuration.getWorldSettings(oponent.location.world.name).mobSettings

        if (SpecialMobUtil.isSpecialMob(damager)) return
        val vanillaMobSettings: VanillaMobSettings = settings.vanillaMobSettings

        if (damager is Monster || damager is Boss) {
            event.damage = event.damage * vanillaMobSettings.damageMultiplier
        }
    }

    /*
    Handling of players dealing damage to entities during blood night.
     */
    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        if (!nightManager.isBloodNightActive(event.damager.world)) return

        val sender: ProjectileSender = ProjectileUtil.getProjectileSource(event.damager)
        if ((sender.isEntity && sender.entity !is Player)
            || event.damager !is Player
        ) return
        val oponent: Entity = event.entity as? Player ?: return
        val entityId = oponent.entityId

        if (!(oponent is Monster || oponent is Boss)) return
        if (SpecialMobUtil.isSpecialMob(oponent)) return

        val vanillaMobSettings =
            configuration.getWorldSettings(oponent.location.world.name).mobSettings.vanillaMobSettings
        event.damage /= vanillaMobSettings.healthMultiplier

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityKill(event: EntityDeathEvent) {
        val entity = event.entity
        val player = event.entity.killer

        if (!(entity is Monster || entity is Boss)) return
        if (!nightManager.isBloodNightActive(entity.world)) {
            event.drops.forEach(::removePickupTag); return
        }
        if (entity.persistentDataContainer.has(NO_DROP, PersistentDataType.BYTE)) return

        val worldSettings = configuration.getWorldSettings(entity.world)
        val shockwaveSettings = worldSettings.deathActionSettings.mobDeathActions.shockwaveSettings
        SpecialMobUtil.dispatchShockwave(shockwaveSettings, event.entity.location)
        SpecialMobUtil.dispatchLightning(
            worldSettings.deathActionSettings.mobDeathActions.lightningSettings,
            event.entity.location
        )

        val mobSettings: MobSettings = worldSettings.mobSettings
        val vanillaMobSettings: VanillaMobSettings = mobSettings.vanillaMobSettings
        event.droppedExp *= mobSettings.experienceMultiplier.toInt()

        if (player == null) {
            BloodNight.logger()
                .fine("Entity ${entity.customName} was not killed by a player."); specialMobManager.remove(event.entity); return
        }

        if (SpecialMobUtil.isExtension(entity)) {
            BloodNight.logger().finer("Mob is extension. Ignore."); return
        }

        BloodNight.logger().fine("Entity " + entity.customName + " was killed by " + player.name)
        BloodNight.logger().fine("Attempt to drop items.")

        if (SpecialMobUtil.isSpecialMob(entity)) {
//            if (!mobSettings.naturalDrops) {
//                BloodNight.logger().fine("Natural Drops are disabled. Clear loot.")
//                event.drops.clear()
//            } else {
//                BloodNight.logger().fine("Natural Drops are enabled. Multiply loot.")
//                for (drop in event.drops) {
//                    if (isPickedUp(drop)) continue
//                    drop.amount *= vanillaMobSettings.dropMultiplier.toInt()
//                }
//            }

            val specialMob = SpecialMobUtil.getSpecialMobType(entity) ?: return
            val mobByName = mobSettings.getMobByName(specialMob) ?: return
            val drops = mobSettings.getDrops(mobByName)
            BloodNight.logger().finer("Added ${drops.size} drops to ${event.drops.size} drops.")
            event.drops.addAll(drops)
//        } else {
//            val dropMode: VanillaDropMode = vanillaMobSettings.vanillaDropMode
//            when (dropMode) {
//                VanillaDropMode.VANILLA -> event.drops
//                    .filterNot(::isPickedUp)
//                    .forEach { it.amount *= vanillaMobSettings.dropMultiplier.toInt() }
//                VanillaDropMode.COMBINE -> {
//                    event.drops
//                        .filterNot(::isPickedUp)
//                        .forEach { it.amount *= vanillaMobSettings.dropMultiplier.toInt() }
//                    event.drops.addAll(mobSettings.getDrops(vanillaMobSettings.extraDrops))
//                }
//                VanillaDropMode.CUSTOM -> {
//                    event.drops.clear()
//                    event.drops.addAll(mobSettings.getDrops(vanillaMobSettings.extraDrops))
//                }
//            }
//            if (dropMode !== VanillaDropMode.VANILLA && vanillaMobSettings.extraDrops > 0) {
//                val drops: List<ItemStack> = mobSettings.getDrops(vanillaMobSettings.extraDrops)
//                event.drops.addAll(drops)
//            }
//        }
//    }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onEntityExplode(event: EntityExplodeEvent) {
        val worldSettings: WorldSettings = configuration.getWorldSettings(event.location.world)

        if (!worldSettings.enabled) return
        if (event.entity.type != EntityType.CREEPER) return

        val bloodNightActive: Boolean = nightManager.isBloodNightActive(event.location.world)

        if ((worldSettings.alwaysManageCreepers || bloodNightActive)
            && !worldSettings.creeperBlockDamage
        ) {
            val size: Int = event.blockList().size
            event.blockList().clear()
            BloodNight.logger().finest("Explosion is canceled? ${event.isCancelled}")
            BloodNight.logger().finest("Prevented $size from destruction")
        }
        delayedActions.schedule({ specialMobManager.remove(event.entity) }, 1)
    }

    @EventHandler
    private fun onChunkLoad(event: ChunkLoadEvent) {
        val worldMobs = specialMobManager.getWorldMobs(event.world)
        event.chunk.entities.forEach {
            val remove = worldMobs.remove(it.uniqueId)
            if (!SpecialMobUtil.isSpecialMob(it)) return
            if (remove != null) remove.remove() else it.remove()
        }
        if (!configuration.generalSettings.beeFix) return
        val hives = AtomicInteger(0)
        val entities = AtomicInteger(0)

        // Bugfix for the sin of flying creepers with bees.
        IteratingTask(
            listOf(*event.chunk.tileEntities), label@
            {
                if (it !is Beehive) return@label false
                hives.incrementAndGet()
                it.releaseEntities().forEach { b ->
                    entities.incrementAndGet()
                    BloodNight.logger().finer("Checking entity with id ${b.entityId} and ${b.uniqueId}")
                    if (SpecialMobUtil.isSpecialMob(b)) b.remove()
                    return@label true
                }
                it.update(true)
                var newState = it.block.state as Beehive
                if (newState.entityCount != 0) {
                    BloodNight.logger().config("Bee Hive is not empty but should.")
                    val blockData = it.blockData
                    it.block.type = Material.AIR
                    it.block.type = it.type
                    it.blockData = blockData
                    newState = it.block.state as Beehive
                    if (newState.entityCount != 0) {
                        BloodNight.logger().config("§cBee Hive is still not empty but should.")
                    } else BloodNight.logger().config("§2Bee Hive is empty now.")
                }
                false
            },
            { s ->
                if (hives.get() != 0) {
                    BloodNight.logger()
                        .fine("Checked ${hives.get()} Hive/s with ${entities.get()} Entities and removed ${s.processedElements} lost bees in ${s.time}ms.")
                }
            }).runTaskTimer(BloodNight.instance, 0, 1)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onItemPickup(event: EntityPickupItemEvent) {
        if (event.entity is Player) {
            removePickupTag(event.item.itemStack)
            return
        }
        if (!configuration.getWorldSettings(event.entity.world).enabled) return
        addPickupTag(event.item.itemStack)
    }

    fun onHopperPickUp(event: InventoryPickupItemEvent) {
        removePickupTag(event.item.itemStack)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onItemDrop(event: EntityDropItemEvent) {
        if (event.entity is Player) return
        if (!configuration.getWorldSettings(event.entity.world).enabled) return
        removePickupTag(event.itemDrop.itemStack)
    }

    private fun addPickupTag(itemStack: ItemStack) {
        DataContainerUtil.setIfAbsent(
            itemStack,
            PICKED_UP,
            PersistentDataType.BYTE,
            DataContainerUtil.booleanToByte(true)
        )
    }

    private fun removePickupTag(itemStack: ItemStack) {
        DataContainerUtil.remove(itemStack, PICKED_UP, PersistentDataType.BYTE)
    }

    private fun isPickedUp(itemStack: ItemStack): Boolean {
        return DataContainerUtil.hasKey(itemStack, PICKED_UP, PersistentDataType.BYTE)
    }
}