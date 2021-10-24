package me.racci.bloodnight.hooks

import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.core.manager.nightmanager.NightManager
import me.racci.bloodnight.hooks.placeholderapi.PlaceholderAPIHook
import me.racci.bloodnight.hooks.worldmanager.MultiverseHook
import me.racci.bloodnight.hooks.worldmanager.WorldManager
import me.racci.raccicore.utils.pm
import org.bukkit.plugin.Plugin
import java.util.concurrent.Callable
import java.util.logging.Level

class HookService(private val plugin: Plugin, configuration: Configuration, nightManager: NightManager) {
    private val hooks: MutableMap<Class<*>, AbstractHookService<*>> = HashMap()
    private val configuration: Configuration
    private val nightManager: NightManager
    fun setup() {
        add("PlaceholderAPI")   { PlaceholderAPIHook() }
        add("Multiverse-Core")  { MultiverseHook() }
    }

    fun add(name: String, hook: Callable<AbstractHookService<*>>) {
        if (!pm.isPluginEnabled(name)) {
            plugin.logger.info("Hook into $name failed. Plugin is not enabled.")
            return
        }
        val call: AbstractHookService<*> = try {
            hook.call()
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed to create hook for $name. Is the plugin up to date?")
            return
        }
        if (call.active) {
            call.setup()
            hooks[call.javaClass] = call
        }
        plugin.logger.info("Hook into $name successful.")
    }

    fun shutdown() {
        BloodNight.logger().info("Hooks shutting down.")
        hooks.values.forEach{it.shutdown()} ; hooks.clear()
    }

    val worldManager: WorldManager
        get() {
            return if (hooks.containsKey(MultiverseHook::class.java)) {
                hooks[MultiverseHook::class.java] as WorldManager
            } else WorldManager.DEFAULT
        }

    init {
        this.configuration = configuration
        this.nightManager = nightManager
    }
}