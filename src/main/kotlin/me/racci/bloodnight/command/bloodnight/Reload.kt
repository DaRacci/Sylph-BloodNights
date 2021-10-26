package me.racci.bloodnight.command.bloodnight

import de.eldoria.eldoutilities.simplecommands.EldoCommand
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.util.Permissions
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin

class Reload(plugin: Plugin) : EldoCommand(plugin) {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (denyAccess(sender, Permissions.Admin.RELOAD)) {
            return true
        }
        BloodNight.instance.onReload()
        messageSender().sendMessage(sender, localizer().getMessage("reload.success"))
        BloodNight.logger().info("BloodNight reloaded!")
        return true
    }
}