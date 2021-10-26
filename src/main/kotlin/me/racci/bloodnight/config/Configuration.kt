package me.racci.bloodnight.config

import de.eldoria.eldoutilities.configuration.EldoConfig
import me.racci.bloodnight.config.generalsettings.GeneralSettings
import me.racci.bloodnight.config.worldsettings.WorldSettings
import me.racci.bloodnight.core.BloodNight
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin
import java.nio.file.Paths
import java.util.function.Function

class Configuration(plugin: Plugin) : EldoConfig(plugin) {

    val worldSettings = HashMap<String, WorldSettings>()
    lateinit var generalSettings: GeneralSettings

    fun getWorldSettings(key: World) =
        loadWorldSettings(key.name, false)

    fun getWorldSettings(key: String) =
        loadWorldSettings(key, false)

    override fun reloadConfigs() {
        BloodNight.logger().info("Loading config.")
        if (version == -1) {
            initV2()
            save()
            reloadConfigs()
            return
        }
        if (version <= 1) {
            BloodNight.logger().info("§2Migrating config to v2")
            migrateToV2()
            BloodNight.logger().info("§2Migration of config to v2 done.")
        }
        generalSettings = mainConfig.config.getObject("generalSettings", GeneralSettings::class.java)!!
        Bukkit.getWorlds().map(World::getName).forEach(::loadWorldSettings)
        save()
    }

    private fun initV2() {
        BloodNight.logger().info("§eConfig is empty. Rebuilding config")
        setVersion(2, false)
        BloodNight.logger().info("§2Config version 2")
        generalSettings = GeneralSettings()
        BloodNight.logger().info("§2Added general settings")
        BloodNight.logger().info("§2Config initialized")
    }

    private fun migrateToV2() {
        setVersion(2, false)
        mainConfig.config["updateReminder"] = null
        val worldList =
            mainConfig.config["worldSettings", ArrayList<WorldSettings>()] as? ArrayList<WorldSettings> ?: ArrayList()
        val worldSettings = Paths.get(plugin.dataFolder.toPath().toString(), "worldSettings")
        worldList.forEach {
            loadConfig(
                getWorldConfigPath(it.worldName),
                { s: FileConfiguration ->
                    s["version"] = 1
                    s["settings"] = it
                }, true
            ); BloodNight.logger().info("§2Migrated settings for ${it.worldName}")

        }
        mainConfig.config["worldSettings"] = null
    }

    // Make sure using true as default is ok
    private fun loadWorldSettings(world: String, reload: Boolean = true): WorldSettings {
        return if (reload) {
            worldSettings.compute(world) { _, _ ->
                val config = loadConfig(getWorldConfigPath(world), { c ->
                    c["version"] = 1
                    c["settings"] = WorldSettings(world)
                }, true); config.getObject("settings", WorldSettings::class.java, WorldSettings(world))
            }!!
        } else {
            worldSettings.computeIfAbsent(world, Function<String, WorldSettings> {
                val config = loadConfig(getWorldConfigPath(world), { c ->
                    c["version"] = 1
                    c["settings"] = WorldSettings(world)
                }, false); config.getObject("settings", WorldSettings::class.java, WorldSettings(world))
            })
        }
    }

    override fun saveConfigs() {
        mainConfig.config.set("generalSettings", generalSettings)
        worldSettings.entries
            .forEach {
                loadConfig(getWorldConfigPath(it.key), null, false).apply {
                    this["settings"] = it.value
                }
            }
        BloodNight.logger().config("Saved config.")
    }

    private fun getWorldConfigPath(world: String): String {
        return "worldSettings/$world"
    }
}