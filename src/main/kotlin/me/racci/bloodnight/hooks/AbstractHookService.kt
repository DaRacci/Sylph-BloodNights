package me.racci.bloodnight.hooks

import org.bukkit.Bukkit

/**
 * A hook for a plugin.
 *
 * @param <T> type of plugin hook.
</T> */
abstract class AbstractHookService<T>(val name: String) {
    /**
     * True if the plugin of this hook is enabled.
     */
    val active: Boolean = Bukkit.getPluginManager().isPluginEnabled(name)

    /**
     * Get the Hook of the Plugin.
     *
     * @return hook instance.
     * @throws ClassNotFoundException when the plugin is not loaded.
     */
    @get:Throws(ClassNotFoundException::class)
    abstract val hook: T

    /**
     * Initialize the hook
     */
    abstract fun setup()

    /**
     * Shutdown the hook and stop any attached services.
     */
    abstract fun shutdown()

}