package me.racci.bloodnight.command.bloodnight

import de.eldoria.eldoutilities.simplecommands.EldoCommand
import de.eldoria.eldoutilities.utils.ArrayUtil
import me.racci.bloodnight.core.manager.mobmanager.MobManager
import me.racci.bloodnight.core.manager.nightmanager.NightManager
import me.racci.bloodnight.core.mobfactory.MobFactory
import me.racci.bloodnight.core.mobfactory.SpecialMobRegistry
import me.racci.bloodnight.util.Permissions
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.stream.Collectors

class SpawnMob(plugin: Plugin, nightManager: NightManager, mobManager: MobManager) : EldoCommand(plugin) {

    private val nightManager: NightManager
    private val mobManager: MobManager

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            messageSender().sendError(sender, localizer().getMessage("error.console"))
            return true
        }
        if (denyAccess(sender, Permissions.Admin.SPAWN_MOB)) {
            return true
        }
        if (args.isEmpty()) {
            messageSender().sendError(sender, "invalid syntax")
            return true
        }
        val player: Player = sender
        if (nightManager.isBloodNightActive(player.world)) {
            val targetBlock: Block = player.getTargetBlock(null, 100)
            if (targetBlock.type == Material.AIR) {
                messageSender().sendError(player, "No Block in sight.")
                return true
            }
            val mobFactory = SpecialMobRegistry.getMobFactoryByName(args[0])
            if (mobFactory == null) {
                messageSender().sendError(player, "Invalid mob type")
                return true
            }
            val entity =
                targetBlock.world.spawnEntity(targetBlock.location.add(0.0, 1.0, 0.0), mobFactory.entityType)
            MobManager.specialMobManager.wrapMob(entity, mobFactory)
        } else {
            messageSender().sendError(player, "no blood night active")
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String>? {
        if (args.size == 1) {
            val strings: Array<String> = SpecialMobRegistry.registeredMobs
                .map(MobFactory::mobName)
                .toTypedArray()
            return ArrayUtil.startingWithInArray(args[0], strings)
                .collect(Collectors.toList())
        }
        return emptyList()
    }

    init {
        this.nightManager = nightManager
        this.mobManager = mobManager
    }
}