package me.racci.bloodnight.command

import me.racci.bloodnight.config.Configuration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import java.util.*

class InventoryListener(configuration: Configuration) : Listener {

    private val inventories = HashMap<UUID, InventoryActionHandler>()
    private val configuration: Configuration

    fun registerModification(player: Player, handler: InventoryActionHandler) {
        inventories[player.uniqueId] = handler
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (inventories.containsKey(event.player.uniqueId)) {
            inventories.remove(event.player.uniqueId)!!.onInventoryClose(event)
            configuration.save()
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (inventories.containsKey(event.whoClicked.uniqueId)) {
            inventories[event.whoClicked.uniqueId]!!.onInventoryClick(event)
        }
    }

    interface InventoryActionHandler {
        fun onInventoryClose(event: InventoryCloseEvent)
        fun onInventoryClick(event: InventoryClickEvent)
    }

    init {
        this.configuration = configuration
    }
}