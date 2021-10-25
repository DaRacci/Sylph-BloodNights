package me.racci.bloodnight.core.manager

import de.eldoria.eldoutilities.localization.ILocalizer
import de.eldoria.eldoutilities.localization.Replacement
import de.eldoria.eldoutilities.messages.MessageSender
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.config.generalsettings.BroadcastLevel
import me.racci.bloodnight.config.generalsettings.BroadcastMethod
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.core.api.BloodNightBeginEvent
import me.racci.bloodnight.core.api.BloodNightEndEvent
import me.racci.bloodnight.core.manager.nightmanager.NightManager
import me.racci.bloodnight.hooks.HookService
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent

class NotificationManager(val configuration: Configuration, val nightManager: NightManager, val hookService: HookService) : Listener {

    val localizer       = ILocalizer.getPluginLocalizer(BloodNight::class.java)
    val messageSender   = MessageSender.getPluginMessageSender(BloodNight::class.java)

    @EventHandler(priority = EventPriority.MONITOR)
    fun onBloodNightEnd(event: BloodNightEndEvent) {
        dispatchBroadcast(
            event.world,
            localizer.getMessage(
                "notify.nightEnd",
                Replacement.create("WORLD", getAlias(event.world)).addFormatting('6')
            )
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBloodNightStart(event: BloodNightBeginEvent) {
        dispatchBroadcast(
            event.world,
            localizer.getMessage(
                "notify.nightStart",
                Replacement.create("WORLD", getAlias(event.world)).addFormatting('6')
            )
        )
    }

    private fun dispatchBroadcast(world: World, message: String) {
        val players: Collection<Player> = when (configuration.generalSettings.broadcastLevel) {
            BroadcastLevel.SERVER -> Bukkit.getOnlinePlayers()
            BroadcastLevel.WORLD -> world.players
            BroadcastLevel.NONE -> return
        }
        players.forEach{sendBroadcast(it, message)}
    }

    private fun sendBroadcast(player: Player, message: String) {
        val m = "§a" + message.replace("§r", "§r§a")
        when (configuration.generalSettings.broadcastMethod) {
            BroadcastMethod.CHAT -> messageSender.sendMessage(player, message)
            BroadcastMethod.TITLE -> player.sendTitle(m, "", 10, 70, 20)
            BroadcastMethod.SUBTITLE -> player.sendTitle("", m, 10, 70, 20)
        }
    }

    private fun sendMessage(player: Player, message: String) {
        val m = "§a" + message.replace("§r", "§r§a")
        when (configuration.generalSettings.messageMethod) {
            BroadcastMethod.CHAT -> messageSender.sendMessage(player, message)
            BroadcastMethod.TITLE -> player.sendTitle(m, "", 10, 70, 20)
            BroadcastMethod.SUBTITLE -> player.sendTitle("", m, 10, 70, 20)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerWorldChange(event: PlayerChangedWorldEvent) {
        if (!configuration.generalSettings.joinWorldWarning) return
        val origin: Boolean = nightManager.isBloodNightActive(event.from)
        val destination: Boolean = nightManager.isBloodNightActive(event.player.world)
        if (destination) {
            sendMessage(event.player, localizer.getMessage("notify.bloodNightJoined"))
            return
        }
        if (origin) {
            sendMessage(event.player, localizer.getMessage("notify.bloodNightLeft"))
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!configuration.generalSettings.joinWorldWarning) return
        if (nightManager.isBloodNightActive(event.player.world)) {
            sendMessage(event.player, localizer.getMessage("notify.bloodNightJoined"))
        }
    }

    fun getAlias(world: World): String {
        return hookService.worldManager.getAlias(world)
    }
}