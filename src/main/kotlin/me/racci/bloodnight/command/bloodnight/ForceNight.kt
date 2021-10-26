package me.racci.bloodnight.command.bloodnight

import de.eldoria.eldoutilities.localization.Replacement
import de.eldoria.eldoutilities.simplecommands.EldoCommand
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil
import de.eldoria.eldoutilities.utils.ArgumentUtils
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.core.manager.nightmanager.NightManager
import me.racci.bloodnight.util.Permissions
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class ForceNight(plugin: Plugin?, nightManager: NightManager, configuration: Configuration) : EldoCommand(plugin) {

    private val nightManager: NightManager
    private val configuration: Configuration

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (denyAccess(sender, Permissions.Admin.FORCE_NIGHT)) {
            return true
        }
        var world: World? = null
        if (sender is Player) {
            val player: Player = sender
            world = player.world
        } else {
            if (argumentsInvalid(sender, args, 1, "[" + localizer().getMessage("syntax.worldName") + "]")) {
                return true
            }
        }
        world = ArgumentUtils.getOrDefault<World>(
            args,
            0,
            { ArgumentUtils.getWorld(it) },
            world
        )
        if (world == null) {
            messageSender().sendError(sender, localizer().getMessage("error.invalidWorld"))
            return true
        }
        val enabled: Boolean = configuration.getWorldSettings(world).enabled
        if (!enabled) {
            messageSender().sendError(
                sender, localizer().getMessage(
                    "error.worldNotEnabled",
                    Replacement.create("WORLD", world.name).addFormatting('6')
                )
            )
            return true
        }
        if (!nightManager.bloodWorldsSet.contains(world)) {
            nightManager.forceNight(world)
            messageSender().sendMessage(
                sender, localizer().getMessage(
                    "forceNight.enabled",
                    Replacement.create("WORLD", world.name).addFormatting('6')
                )
            )
        } else {
            messageSender().sendError(
                sender, localizer().getMessage(
                    "forceNight.alreadyActive",
                    Replacement.create("WORLD", world.name).addFormatting('6')
                )
            )
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String>? {
        return if (args.size == 1) {
            TabCompleteUtil.completeWorlds(args[0])
        } else emptyList()
    }

    init {
        this.nightManager = nightManager
        this.configuration = configuration
    }
}