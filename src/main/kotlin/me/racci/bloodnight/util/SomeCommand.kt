package me.racci.bloodnight.util

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerListPingEvent
import kotlin.math.abs

class SomeCommand : CommandExecutor, Listener {
    override fun onCommand(
        commandSender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        val p = commandSender as Player
        val baseLoc = p.location
        val horSize = 2
        val vertSize = 5
        var fire: Location? = null
        for (x in horSize * -1..horSize) {
            for (y in 0..vertSize) {
                val loc = baseLoc.clone().add(x.toDouble(), y.toDouble(), 0.0)
                if (abs(x) == vertSize || y == 0 || y == vertSize) loc.block.type =
                    Material.OBSIDIAN else if (y == 1 && x == 0) fire = loc else loc.block.type = Material.AIR
            }
        }
        fire!!.block.type = Material.FIRE
        return true
    }

    @EventHandler
    fun onServerPing(event: ServerListPingEvent?) {
    }
}