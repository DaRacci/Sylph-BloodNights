package me.racci.bloodnight.core

import de.eldoria.bloodnight.api.IBloodNightAPI
import de.eldoria.eldoutilities.bstats.EldoMetrics
import de.eldoria.eldoutilities.bstats.charts.MultiLineChart
import de.eldoria.eldoutilities.localization.ILocalizer
import de.eldoria.eldoutilities.messages.MessageSender
import de.eldoria.eldoutilities.plugin.EldoPlugin
import de.eldoria.eldoutilities.updater.Updater
import de.eldoria.eldoutilities.updater.butlerupdater.ButlerUpdateData
import me.racci.bloodnight.command.BloodNightCommand
import me.racci.bloodnight.command.InventoryListener
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.config.generalsettings.GeneralSettings
import me.racci.bloodnight.config.worldsettings.BossBarSettings
import me.racci.bloodnight.config.worldsettings.NightSelection
import me.racci.bloodnight.config.worldsettings.NightSettings
import me.racci.bloodnight.config.worldsettings.WorldSettings
import me.racci.bloodnight.config.worldsettings.deathactions.DeathActionSettings
import me.racci.bloodnight.config.worldsettings.deathactions.MobDeathActions
import me.racci.bloodnight.config.worldsettings.deathactions.PlayerDeathActions
import me.racci.bloodnight.config.worldsettings.deathactions.PotionEffectSettings
import me.racci.bloodnight.config.worldsettings.deathactions.subsettings.LightningSettings
import me.racci.bloodnight.config.worldsettings.deathactions.subsettings.ShockwaveSettings
import me.racci.bloodnight.config.worldsettings.mobsettings.Drop
import me.racci.bloodnight.config.worldsettings.mobsettings.MobSetting
import me.racci.bloodnight.config.worldsettings.mobsettings.MobSettings
import me.racci.bloodnight.config.worldsettings.mobsettings.VanillaMobSettings
import me.racci.bloodnight.config.worldsettings.sound.SoundEntry
import me.racci.bloodnight.config.worldsettings.sound.SoundSettings
import me.racci.bloodnight.core.api.BloodNightAPI
import me.racci.bloodnight.core.manager.NotificationManager
import me.racci.bloodnight.core.manager.mobmanager.MobManager
import me.racci.bloodnight.core.manager.nightmanager.CommandBlocker
import me.racci.bloodnight.core.manager.nightmanager.NightManager
import me.racci.bloodnight.core.mobfactory.MobFactory
import me.racci.bloodnight.core.mobfactory.SpecialMobRegistry
import me.racci.bloodnight.hooks.HookService
import org.bukkit.NamespacedKey
import org.checkerframework.checker.units.qual.K
import java.util.*
import java.util.concurrent.Callable
import java.util.function.Function
import java.util.logging.Logger
import java.util.stream.Collectors
import kotlin.collections.HashMap

class BloodNight : EldoPlugin() {

    companion object {

        lateinit var instance            : BloodNight           ; private set
        lateinit var nightManager        : NightManager         ; private set
        lateinit var mobManager          : MobManager           ; private set
        lateinit var configuration       : Configuration        ; private set
        lateinit var inventoryListener   : InventoryListener    ; private set
        lateinit var bloodNightAPI       : BloodNightAPI        ; private set
        lateinit var hookService         : HookService          ; private set
        var initialized                  = false                ; private set

        fun logger() =
            instance.logger

    }

    override fun onPluginEnable(reload: Boolean) {
        if (!initialized) {
            instance = this
            setLoggerLevel()
            configuration = Configuration(this)
            val localizer: ILocalizer = ILocalizer.create(this, "en_US")
            val mobLocaleCodes: Map<String, String> = SpecialMobRegistry.getRegisteredMobs().stream()
                .map(MobFactory::getMobName)
                .collect(
                    Collectors.toMap(
                        Function<T, K> { k: T -> "mob.$k" },
                        Function<T, U> { k: T -> java.lang.String.join(" ", k.split("(?<=.)(?=\\p{Lu})")) })
                )
            localizer.addLocaleCodes(mobLocaleCodes)
            localizer.setLocale(configuration.getGeneralSettings().getLanguage())
            MessageSender.create(this, configuration.getGeneralSettings().getPrefix())
            registerListener()
            bloodNightAPI = BloodNightAPI(nightManager, configuration)
            registerCommand(
                "bloodnight",
                BloodNightCommand(configuration, this, nightManager, mobManager, inventoryListener)
            )
            enableMetrics()
            if (configuration.getGeneralSettings().isUpdateReminder()) {
                Updater.butler(
                    ButlerUpdateData(
                        this, Permissions.Admin.RELOAD, true,
                        configuration.getGeneralSettings().isAutoUpdater(), 4, "https://plugins.eldoria.de"
                    )
                )
                    .start()
            }
            hookService = HookService(this, configuration, nightManager)
            hookService.setup()
            lateInit()
        }
        onReload()
        if (initialized) {
            logger().info("§2BloodNight reloaded!")
        } else {
            logger().info("§2BloodNight enabled!")
            initialized = true
        }
        val bloodNightAPI: IBloodNightAPI? = getBloodNightAPI()
    }

    fun onReload() {
        configuration.reload()
        ILocalizer.getPluginLocalizer(this).setLocale(configuration.getGeneralSettings().getLanguage())
        logger().config("§cDebug mode active")
        nightManager.reload()
    }

    private fun lateInit() {
        registerListener(NotificationManager(configuration, nightManager, hookService))
    }

    private fun registerListener() {
        nightManager = NightManager(configuration)
        nightManager.runTaskTimer(this, 5, 1)
        mobManager = MobManager(nightManager, configuration)
        inventoryListener = InventoryListener(configuration)
        val commandBlocker = CommandBlocker(nightManager, configuration)
        registerListener(commandBlocker, inventoryListener, mobManager, nightManager)
    }

    val configSerialization: List<Class<out Any?>>
        get() = Arrays.asList(
            GeneralSettings::class.java,
            NightSelection::class.java,
            NightSettings::class.java,
            MobSettings::class.java,
            MobSetting::class.java,
            VanillaMobSettings::class.java,
            WorldSettings::class.java,
            Drop::class.java,
            BossBarSettings::class.java,
            MobSettings.MobTypes::class.java,
            SoundSettings::class.java,
            SoundEntry::class.java,
            PotionEffectSettings::class.java,
            PlayerDeathActions::class.java,
            MobDeathActions::class.java,
            LightningSettings::class.java,
            ShockwaveSettings::class.java,
            DeathActionSettings::class.java
        )

    private fun enableMetrics() {
        val metrics = EldoMetrics(this, 9123)
        if (metrics.isEnabled()) {
            logger().info("§2Metrics enabled. Thank you! (> ^_^ )>")
            metrics.addCustomChart(MultiLineChart("update_settings", label@ Callable<Map<String, Int>> {
                val map: MutableMap<String, Int> = HashMap()
                map["Update Check"] = if (configuration.getGeneralSettings().isUpdateReminder()) 1 else 0
                if (configuration.getGeneralSettings().isUpdateReminder()) {
                    map["Auto Update"] = if (configuration.getGeneralSettings().isUpdateReminder()) 1 else 0
                    return@label map
                }
                map["Auto Update"] = 0
                map
            }))
            metrics.addCustomChart(MultiLineChart("mob_types", Callable<Map<String, Int>> {
                val map: MutableMap<String, Int> = HashMap()
                for (factory in SpecialMobRegistry.getRegisteredMobs()) {
                    for (world in configuration.getWorldSettings().values()) {
                        if (!world.isEnabled()) continue
                        val mobByName: Optional<MobSetting> = world.getMobSettings().getMobByName(factory.getMobName())
                        map.compute(mobByName.get().getMobName()) { key: String?, value: Int? ->
                            if (value == null) {
                                return@compute if (mobByName.get().isActive()) 1 else 0
                            }
                            value + if (mobByName.get().isActive()) 1 else 0
                        }
                    }
                }
                map
            }))
            metrics.addCustomChart(MultiLineChart("night_selection", Callable<Map<String, Int>> {
                val map: MutableMap<String, Int> = HashMap()
                for (world in configuration.getWorldSettings().values()) {
                    if (!world.isEnabled()) continue
                    map.compute(
                        world.getNightSelection().getNightSelectionType().toString()
                    ) { key: String?, value: Int? ->
                        if (value == null) {
                            return@compute 1
                        }
                        value + 1
                    }
                }
                map
            }))
            return
        }
        logger().info("§2Metrics are not enabled. Metrics help me to stay motivated. Please enable it.")
    }

    override fun onPluginDisable() {
        if (nightManager != null) {
            nightManager.shutdown()
        }
        if (hookService != null) {
            hookService.shutdown()
        }
        logger().info("Blood Night disabled!")
    }

    companion object {
        private var instance: BloodNight? = null
        fun getNamespacedKey(string: String): NamespacedKey {
            return NamespacedKey(instance, string.replace(" ", "_"))
        }

        fun logger(): Logger {
            return EldoPlugin.getInstance().getLogger()
        }

        fun getBloodNightAPI(): IBloodNightAPI? {
            return instance!!.bloodNightAPI
        }
    }
}