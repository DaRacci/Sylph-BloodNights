package me.racci.bloodnight.command.bloodnight

import de.eldoria.eldoutilities.localization.Replacement
import de.eldoria.eldoutilities.simplecommands.EldoCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin

class About(plugin: Plugin) : EldoCommand(plugin) {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val description = plugin.description
        val info = localizer().getMessage(
            "about",
            Replacement.create("PLUGIN_NAME", "Blood Night").addFormatting('b'),
            Replacement.create("AUTHORS", java.lang.String.join(", ", description.authors)).addFormatting('b'),
            Replacement.create("VERSION", description.version).addFormatting('b'),
            Replacement.create("WEBSITE", description.website).addFormatting('b'),
            Replacement.create("DISCORD", "https://discord.gg/3bYny67").addFormatting('b')
        )
        messageSender().sendMessage(sender, info)
        return true
    }
}