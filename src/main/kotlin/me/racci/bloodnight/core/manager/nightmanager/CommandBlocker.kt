package me.racci.bloodnight.core.manager.nightmanager

import de.eldoria.eldoutilities.messages.MessageSender
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.util.Permissions
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import java.util.*

class CommandBlocker(private val nightManager: NightManager, val configuration: Configuration) : Listener {

    val sender: MessageSender = MessageSender.getPluginMessageSender(BloodNight::class.java)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCommandPreprocessEvent(event: PlayerCommandPreprocessEvent) {
        if (!nightManager.isBloodNightActive(event.player.world)) return
        if (isBlocked(event.message) && !event.player.hasPermission(Permissions.Bypass.COMMAND_BLOCK)) {
            sender.sendLocalizedError(event.player, "error.commandBlocked")
            event.isCancelled = true
        }
    }

    private fun isBlocked(command: String): Boolean {
        val lowerCommand = command.lowercase(Locale.getDefault()).substring(1, command.length)
        for (blockedCommand in configuration.generalSettings.blockedCommands) {
            if (lowerCommand.startsWith(blockedCommand.lowercase(Locale.getDefault()))) {
                return true
            }
        }
        return false
    }
}