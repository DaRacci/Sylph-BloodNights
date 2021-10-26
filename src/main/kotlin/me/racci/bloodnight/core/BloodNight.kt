package me.racci.bloodnight.core

import de.eldoria.eldoutilities.localization.ILocalizer
import de.eldoria.eldoutilities.messages.MessageSender
import de.eldoria.eldoutilities.plugin.EldoPlugin
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
import me.racci.bloodnight.core.api.IBloodNightAPI
import me.racci.bloodnight.core.manager.NotificationManager
import me.racci.bloodnight.core.manager.mobmanager.MobManager
import me.racci.bloodnight.core.manager.nightmanager.CommandBlocker
import me.racci.bloodnight.core.manager.nightmanager.NightManager
import me.racci.bloodnight.core.mobfactory.MobFactory
import me.racci.bloodnight.core.mobfactory.SpecialMobRegistry
import me.racci.bloodnight.hooks.HookService
import org.bukkit.NamespacedKey
import java.util.stream.Collectors

class BloodNight : EldoPlugin() {

    companion object {

        private fun isNightManagerInitialized() =
            ::nightManager.isInitialized

        private fun isHookServiceInitialized() =
            ::hookService.isInitialized

        lateinit var instance: BloodNight; private set
        lateinit var mobManager: MobManager; private set
        lateinit var configuration: Configuration; private set
        lateinit var inventoryListener: InventoryListener; private set
        lateinit var bloodNightAPI: IBloodNightAPI; private set
        lateinit var hookService: HookService; private set
        lateinit var nightManager: NightManager; private set
        var initialized = false; private set

        fun logger() =
            instance.logger

        fun namespacedKey(string: String) =
            NamespacedKey(instance, string.replace(" ", "_"))

    }

    override fun onPluginEnable(reload: Boolean) {
        if (!initialized) {
            instance = this
            setLoggerLevel()
            configuration = Configuration(this)
            val localizer: ILocalizer = ILocalizer.create(this, "en_US")
            val mobLocaleCodes = SpecialMobRegistry.registeredMobs.stream()
                .map(MobFactory::mobName)
                .collect(
                    Collectors.toMap(
                        { "mob.$it" },
                        { it.split("(?<=.)(?=\\p{Lu})").joinToString(" ") })
                )
            localizer.addLocaleCodes(mobLocaleCodes)
            localizer.setLocale(configuration.generalSettings.language)
            MessageSender.create(this, configuration.generalSettings.prefix)
            registerListener()
            bloodNightAPI = BloodNightAPI(nightManager, configuration)
            registerCommand(
                "bloodnight",
                BloodNightCommand(configuration, this, nightManager, mobManager, inventoryListener)
            )
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
        val bloodNightAPI: IBloodNightAPI = bloodNightAPI
    }

    fun onReload() {
        configuration.reload()
        ILocalizer.getPluginLocalizer(this).setLocale(configuration.generalSettings.language)
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

    override fun getConfigSerialization() =
        listOf(
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

    override fun onPluginDisable() {
        if (isNightManagerInitialized()) nightManager.shutdown()
        if (isHookServiceInitialized()) hookService.shutdown()
        logger().info("Blood Night disabled!")
    }
}