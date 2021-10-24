package me.racci.bloodnight.hooks.placeholderapi

import me.clip.placeholderapi.PlaceholderAPIPlugin
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.hooks.AbstractHookService

class PlaceholderAPIHook : AbstractHookService<PlaceholderAPIPlugin>("PlaceholderAPI") {

    private lateinit var placeholders: Placeholders

    override val hook: PlaceholderAPIPlugin
        get() = PlaceholderAPIPlugin.getInstance()

    override fun setup() {
        placeholders = Placeholders()
        placeholders.register()
    }

    override fun shutdown() {
        try {
            placeholders.unregister()
        } catch (e: NoSuchMethodError) {
            BloodNight.logger().warning("You are using a legacy version of PlaceholderAPI. Please consider updating.")
        }
    }
}