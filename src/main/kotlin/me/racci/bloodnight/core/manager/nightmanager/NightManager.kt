package me.racci.bloodnight.core.manager.nightmanager

import de.eldoria.eldoutilities.core.EldoUtilities
import de.eldoria.eldoutilities.localization.ILocalizer
import de.eldoria.eldoutilities.messages.MessageSender
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.config.worldsettings.BossBarSettings
import me.racci.bloodnight.config.worldsettings.WorldSettings
import me.racci.bloodnight.config.worldsettings.deathactions.PlayerDeathActions
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.core.api.BloodNightBeginEvent
import me.racci.bloodnight.core.api.BloodNightEndEvent
import me.racci.bloodnight.core.manager.nightmanager.util.BloodNightData
import me.racci.bloodnight.core.manager.nightmanager.util.NightUtil
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.util.getBossBarNamespace
import me.racci.raccicore.utils.catch
import me.racci.raccicore.utils.console
import me.racci.raccicore.utils.extensions.pluginManager
import org.bukkit.Bukkit
import org.bukkit.Bukkit.dispatchCommand
import org.bukkit.GameRule
import org.bukkit.Statistic
import org.bukkit.World
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class NightManager(private val configuration: Configuration) : BukkitRunnable(), Listener {


    /**
     * A set containing all world where a blood night is active.
     */
    private val bloodWorlds                         = HashMap<World, BloodNightData>()
    private val startNight      : Queue<World>      = ArrayDeque()
    private val endNight        : Queue<World>      = ArrayDeque()
    private val forceNights     : HashSet<World>    = HashSet()
    val localizer               : ILocalizer        = ILocalizer.getPluginLocalizer(BloodNight::class.java)
    val messageSender           : MessageSender     = MessageSender.getPluginMessageSender(BloodNight::class.java)
    private var timeManager     : TimeManager?      = null
    private var soundManager    : SoundManager?     = null
    private var initialized = false
    // <--- Refresh routine ---> //
    /**
     * Check if a day becomes a night.
     */
    override fun run() {
        if (!initialized) {
            cleanup()
            BloodNight.logger().info("Night manager initialized.")
            initialized = true
        }
        changeNightStates()
    }

    private fun cleanup() {
        BloodNight.logger().info("Executing cleanup task on startup.")
        val s = BloodNight.instance.name.lowercase()
        var i = 0
        catch<Exception>() {
            Bukkit.getBossBars().forEach {
                it.key.namespace.equals(s, true)
                Bukkit.removeBossBar(it.key)
                i++; BloodNight.logger().config("Removed 1 boss bar ${it.key}")
            }
        }
        BloodNight.logger().info("Removed $i hanging boss bars.")
    }

    private fun changeNightStates() {

        while (startNight.isNotEmpty()) {
            initializeBloodNight(startNight.poll(), false)
        }

        val it = forceNights.iterator()
        while (it.hasNext()) {
            val world = it.next()
            if (!NightUtil.isNight(world, configuration.getWorldSettings(world))) {
                continue
            }
            initializeBloodNight(world, true)
            it.remove()
        }

//        while (forceNights.isNotEmpty()) {
//            forceNights
//                .filter{NightUtil.isNight(it, configuration.getWorldSettings(it))}
//                .forEach{initializeBloodNight(it, true) ; forceNights.remove(it)}
//        }

        while (endNight.isNotEmpty()) {
            resolveBloodNight(endNight.poll())
        }
    }

    // <--- BloodNight activation and deactivation --->
    private fun initializeBloodNight(world: World, force: Boolean = true) {

        val settings: WorldSettings = configuration.getWorldSettings(world.name)
        if (isBloodNightActive(world)) return
        if (!settings.enabled) {
            BloodNight.logger().fine("Blood night in world ${world.name} is not enabled. Will not initialize.")
            return
        }

        // skip the calculation if a night should be forced.
        if (!force) {
            val sel = settings.nightSelection
            val `val` = ThreadLocalRandom.current().nextInt(101)
            sel.upcount()
            BloodNight.logger().config("Evaluating Blood Night State.")
            BloodNight.logger().config(sel.toString())
            val probability: Int = sel.getCurrentProbability(world)
            BloodNight.logger().config("Current probability: $probability")
            if (probability <= 0) return
            if (`val` > probability) return
        }
        val beginEvent = BloodNightBeginEvent(world)
        // A new blood night has begun.
        pluginManager.callEvent(beginEvent)
        if (beginEvent.isCancelled) {
            BloodNight.logger().fine("BloodNight in ${world.name} was cancelled by another plugin.")
            return
        }
        BloodNight.logger().config("BloodNight in ${world.name} activated.")

        var bossBar: BossBar? = null
        val bbS: BossBarSettings = settings.bossBarSettings
        if (bbS.enabled) {
            bossBar = Bukkit.createBossBar(
                getBossBarNamespace(world),
                bbS.title,
                bbS.colour,
                BarStyle.SOLID,
                *bbS.getEffects()
            )
        }
        bloodWorlds[world] = BloodNightData(world, bossBar)
        dispatchCommands(settings.nightSettings.startCommands, world)
        settings.nightSettings.regenerateNightDuration()
        if (settings.nightSettings.isCustomNightDuration) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        }
        for (player in world.players) {
            enableBloodNightForPlayer(player, world)
        }
    }

    private fun resolveBloodNight(world: World) {
        if (!isBloodNightActive(world)) return
        BloodNight.logger().fine("BloodNight in ${world.name} resolved.")
        val settings: WorldSettings = configuration.getWorldSettings(world.name)
        if (settings.nightSettings.isCustomNightDuration) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
            timeManager!!.removeCustomTime(world)
        }
        dispatchCommands(settings.nightSettings.endCommands, world)
        pluginManager.callEvent(BloodNightEndEvent(world))
        for (player in world.players) {
            disableBloodNightForPlayer(player, world)
        }
        removeBloodWorld(world)?.resolveAll()
    }

    private fun dispatchCommands(cmds: List<String>, world: World) {
        cmds.map { it.replace("{world}", world.name) }
            .forEach {
                if (it.contains("{player}")) {
                    world.players.forEach { p ->
                        dispatchCommand(Bukkit.getConsoleSender(), it.replace("{player}", p.name))
                    }
                } else dispatchCommand(Bukkit.getConsoleSender(), it)
            }
    }

    private fun enableBloodNightForPlayer(player: Player, world: World) {
        val bloodNightData = getBloodNightData(world)
        val worldSettings = configuration.getWorldSettings(player.world.name)
        BloodNight.logger().finer("Enabling blood night for player ${player.name}")
        if (worldSettings.mobSettings.forcePhantoms) {
            player.setStatistic(Statistic.TIME_SINCE_REST, 720000)
        }
        if (configuration.generalSettings.blindness) {
            player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 1, false, true))
        }
        worldSettings.soundSettings.playStartSound(player)
        bloodNightData?.addPlayer(player)
    }

    private fun disableBloodNightForPlayer(player: Player, world: World) {
        val worldSettings: WorldSettings = configuration.getWorldSettings(player.world.name)
        BloodNight.logger().finer("Resolving blood night for player " + player.name)
        getBloodNightData(world)?.removePlayer(player)
        if (configuration.generalSettings.blindness) {
            player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 1, false, true))
        }
        worldSettings.soundSettings.playEndSound(player)
    }

    // <--- Player state consistency ---> //
    @EventHandler
    fun onPlayerWorldChange(event: PlayerChangedWorldEvent) {
        if (isBloodNightActive(event.from)) {
            disableBloodNightForPlayer(event.player, event.from)
        } else {
            Bukkit.getBossBar(getBossBarNamespace(event.from))?.removePlayer(event.player)
        }
        if (isBloodNightActive(event.player.world)) {
            enableBloodNightForPlayer(event.player, event.player.world)
        } else {
            Bukkit.getBossBar(getBossBarNamespace(event.player.world))?.removePlayer(event.player)
        }
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        if (isBloodNightActive(event.player.world)) {
            disableBloodNightForPlayer(event.player, event.player.world)
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (isBloodNightActive(event.player.world)) {
            enableBloodNightForPlayer(event.player, event.player.world)
        }
    }

    // <--- Night Listener ---> //
    @EventHandler(priority = EventPriority.LOW)
    fun onBedEnter(event: PlayerBedEnterEvent) {
        if (!isBloodNightActive(event.player.world)) return
        val nightSettings = configuration.getWorldSettings(event.player.world).nightSettings
        if (nightSettings.skippable) return
        messageSender.sendMessage(event.player, localizer.getMessage("notify.youCantSleep"))
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val actions: PlayerDeathActions = configuration.getWorldSettings(event.entity.world)
            .deathActionSettings
            .playerDeathActions
        SpecialMobUtil.dispatchShockwave(actions.shockwaveSettings, event.entity.location)
        SpecialMobUtil.dispatchLightning(actions.lightningSettings, event.entity.location)
        if (actions.loseInvProbability > ThreadLocalRandom.current().nextInt(100)) {
            event.drops.clear()
        }
        if (actions.loseExpProbability > ThreadLocalRandom.current().nextInt(100)) {
            event.droppedExp = 0
        }
        actions.deathCommands.forEach {
            dispatchCommand(console, it.replace("{player}", event.entity.name))
        }
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player
        val actions = configuration.getWorldSettings(player.world)
            .deathActionSettings
            .playerDeathActions
        if (!isBloodNightActive(player.world) || event.isBedSpawn || event.isAnchorSpawn) {
            return
        }
        EldoUtilities.getDelayedActions().schedule({
            for (value in actions.respawnEffects.values) {
                player.addPotionEffect(PotionEffect(value.effectType, value.duration * 20, 1, false))
            }
        }, 1)
    }
    // <--- Utility functions ---> //
    /**
     * Check if a world is current in blood night mode.
     *
     * @param world world to check
     * @return true if world is currently in blood night mode.
     */
    fun isBloodNightActive(world: World): Boolean {
        return bloodWorlds.containsKey(world)
    }

    fun forceNight(world: World) {
        forceNights.add(world)
    }

    fun cancelNight(world: World) {
        endNight.add(world)
    }

    fun shutdown() {
        BloodNight.logger().info("Shutting down night manager.")
        BloodNight.logger().info("Resolving blood nights.")

        // Copy to new collection since the blood worlds are removed on resolve.
        for (world in HashSet(bloodWorlds.keys)) {
            resolveBloodNight(world)
        }
        BloodNight.logger().info("Night manager shutdown successful.")
    }

    fun reload() {
        for (observedWorld in HashSet(bloodWorlds.keys)) {
            resolveBloodNight(observedWorld)
        }
        if (timeManager != null && !timeManager!!.isCancelled) {
            timeManager!!.cancel()
        }
        timeManager = TimeManager(configuration, this)
        BloodNight.instance.registerListener(timeManager)
        timeManager!!.runTaskTimer(BloodNight.instance, 1, 5)
        if (soundManager != null && !soundManager!!.isCancelled) {
            soundManager!!.cancel()
        }
        soundManager = SoundManager(this, configuration)
        soundManager!!.runTaskTimer(BloodNight.instance, 2, 5)
        timeManager!!.reload()
        bloodWorlds.clear()
    }

    private fun addBloodNight(world: World, bloodNightData: BloodNightData) {
        bloodWorlds[world] = bloodNightData
    }

    private fun removeBloodWorld(world: World) =
        bloodWorlds.remove(world)

    private fun getBloodNightData(world: World) =
        bloodWorldsMap[world]

    /**
     * Get an unmodifiable map of blood words. This map should be used for iteration purposes only.
     *
     * @return unmodifiable map of blood words
     */
    val bloodWorldsMap: Map<World, BloodNightData>
        get() = Collections.unmodifiableMap(bloodWorlds)

    /**
     * Gets an unmodifiable Set of blood worlds.
     *
     * @return unmodifiable Set of blood worlds.
     */
    val bloodWorldsSet: HashSet<World>
        get() = bloodWorlds.keys.toHashSet()

    fun startNight(world: World) {
        startNight.add(world)
    }

    fun endNight(world: World) {
        endNight.add(world)
    }

    init {
        reload()
    }
}