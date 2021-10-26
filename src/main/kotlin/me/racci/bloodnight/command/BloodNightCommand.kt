package me.racci.bloodnight.command

import de.eldoria.eldoutilities.simplecommands.EldoCommand
import de.eldoria.eldoutilities.simplecommands.commands.DefaultDebug
import me.racci.bloodnight.command.bloodnight.*
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.core.manager.mobmanager.MobManager
import me.racci.bloodnight.core.manager.nightmanager.NightManager
import me.racci.bloodnight.util.Permissions
import org.bukkit.plugin.Plugin

class BloodNightCommand(
    configuration: Configuration,
    plugin: Plugin,
    nightManager: NightManager,
    mobManager: MobManager,
    inventoryListener: InventoryListener
) : EldoCommand(plugin) {

    init {
        val help = Help(plugin)
        setDefaultCommand(help)
        registerCommand("help", help)
        registerCommand("about", About(plugin))
        registerCommand("spawnMob", SpawnMob(plugin, nightManager, mobManager))
        registerCommand("cancelNight", CancelNight(plugin, nightManager, configuration))
        registerCommand("forceNight", ForceNight(plugin, nightManager, configuration))
        registerCommand("manageWorlds", ManageWorlds(plugin, configuration))
        registerCommand("manageMob", ManageMob(plugin, configuration, inventoryListener))
        registerCommand("manageNight", ManageNight(plugin, configuration))
        registerCommand("manageMobs", ManageMobs(plugin, configuration, inventoryListener))
        registerCommand("nightSelection", ManageNightSelection(plugin, configuration, inventoryListener))
        registerCommand("deathActions", ManageDeathActions(plugin, configuration))
        registerCommand("reload", Reload(plugin))
        registerCommand("debug", DefaultDebug(plugin, Permissions.Admin.RELOAD))
    }

}