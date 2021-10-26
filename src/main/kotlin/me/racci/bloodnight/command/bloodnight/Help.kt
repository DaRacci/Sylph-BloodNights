package me.racci.bloodnight.command.bloodnight

import de.eldoria.eldoutilities.simplecommands.EldoCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin

class Help(plugin: Plugin) : EldoCommand(plugin) {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        messageSender().sendMessage(
            sender, """
     ${localizer().getMessage("help.help")}
     §6/bn about§r
     ${localizer().getMessage("help.about")}
     §6/bn forceNight§r
     ${localizer().getMessage("help.forceNight")}
     §6/bn cancelNight§r
     ${localizer().getMessage("help.cancelNight")}
     §6/bn manageMob§r
     ${localizer().getMessage("help.manageMob")}
     §6/bn manageMobs§r
     ${localizer().getMessage("help.manageMobs")}
     §6/bn manageNight§r
     ${localizer().getMessage("help.manageNight")}
     §6/bn manageWorlds§r
     ${localizer().getMessage("help.manageWorlds")}
     §6/bn nightSelection§r
     ${localizer().getMessage("help.nightSelection")}
     §6/bn reload§r
     ${localizer().getMessage("help.reload")}
     §6/bn spawnMob§r
     ${localizer().getMessage("help.spawnMob")}
     """.trimIndent()
        )
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String> {
        return emptyList()
    }
}