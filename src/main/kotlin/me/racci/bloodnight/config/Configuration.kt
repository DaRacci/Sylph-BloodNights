package me.racci.bloodnight.config

import de.eldoria.eldoutilities.configuration.EldoConfig
import me.racci.bloodnight.config.generalsettings.GeneralSettings
import me.racci.bloodnight.config.worldsettings.WorldSettings
import me.racci.bloodnight.core.BloodNight
import me.racci.raccicore.utils.catch
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin

class Configuration(plugin: Plugin) : EldoConfig(plugin) {

    lateinit var worldSettings : HashMap<String, WorldSettings>
    lateinit var generalSettings: GeneralSettings

    fun getWorldSettings(key: World) =
        loadWorldSettings(key.name, false)

    fun getWorldSettings(key: String) =
        loadWorldSettings(key, false)

    override fun reloadConfigs() {
        BloodNight.logger().info("Loading config.")
        generalSettings = mainConfig.config.getObject("generalSettings", GeneralSettings::class.java)!!
        Bukkit.getWorlds().map(World::getName).forEach{loadWorldSettings(it, true)}
        save()
    }

    // Make sure using true as default is ok
    private fun loadWorldSettings(world: String, reload: Boolean): WorldSettings {
        catch<Exception> {
            if (!::worldSettings.isInitialized) worldSettings = HashMap()
            return if (reload) {
                worldSettings.compute(world) { _, _ ->
                    val config = loadConfig(getWorldConfigPath(world), { c ->
                        c["version"] = 1
                        c["settings"] = WorldSettings(world)
                    }, true); config.getObject("settings", WorldSettings::class.java, WorldSettings(world))
                }!!
            } else {
                worldSettings.computeIfAbsent(world) {
                    val config = loadConfig(getWorldConfigPath(world), { c ->
                        c["version"] = 1
                        c["settings"] = WorldSettings(world)
                    }, false); config.getObject("settings", WorldSettings::class.java, WorldSettings(world))!!
                }
            }
//            if (reload) {
//                return worldSettings.compute(world) { _, _ ->
//                    val config = loadConfig(
//                        getWorldConfigPath(world),
//                        { c: FileConfiguration ->
//                            c.set("version", 1)
//                            c.set("settings", WorldSettings(world))
//                        }, true
//                    )
//                    return@compute config.getObject("settings", WorldSettings::class.java, WorldSettings(world))
//                }!!
//            }
//            return worldSettings.computeIfAbsent(world) {
//                val config = loadConfig(
//                    getWorldConfigPath(world),
//                    { c: FileConfiguration ->
//                        c.set("version", 1)
//                        c.set("settings", WorldSettings(world))
//                    }, false
//                )
//                return@computeIfAbsent config.getObject("settings", WorldSettings::class.java, WorldSettings(world))!!
//            }
        }

        null!!
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