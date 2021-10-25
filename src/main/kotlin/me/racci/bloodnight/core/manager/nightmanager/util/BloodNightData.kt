package me.racci.bloodnight.core.manager.nightmanager.util

import me.racci.bloodnight.config.worldsettings.sound.SoundSettings
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.util.getBossBarNamespace
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class BloodNightData(val world: World, val bossBar: BossBar?) {

    val playerSoundQueue        = PriorityQueue<PlayerSound>{obj,o-> obj.compareTo(o)}
    val playerConsistencyMap    = HashMap<UUID, ConsistencyCache>()

    fun addPlayer(player: Player) {
        bossBar?.addPlayer(player)
        playerConsistencyMap[player.uniqueId] = ConsistencyCache(player)
        playerSoundQueue.add(PlayerSound(player))
    }

    fun removePlayer(player: Player) {
        Bukkit.getBossBar(getBossBarNamespace(world))?.removePlayer(player)
        if (playerConsistencyMap.containsKey(player.uniqueId)) {
            playerConsistencyMap.remove(player.uniqueId)!!.revert(player)
        }
        playerSoundQueue.removeIf{it.player.uniqueId === player.uniqueId }
    }

    /**
     * Plays a random sound to the player in the queue.
     *
     * @param settings sound settings for the current world
     */
    fun playRandomSound(settings: SoundSettings) {
        if (playerSoundQueue.isEmpty()) return
        while (!playerSoundQueue.isEmpty() && playerSoundQueue.peek().isNext()) {
            val sound       = playerSoundQueue.poll()
            if (!sound.player.isOnline) continue
            val player      = sound.player
            val location    = player.location
            val direction   = player.eyeLocation.toVector()
            location.add(direction.multiply(-1))
            BloodNight.logger().config("Played random sound to " + sound.player.name)
            settings.playRandomSound(player, location)
            sound.scheduleNext(settings.waitSeconds)
            playerSoundQueue.offer(sound)
        }
    }

    fun resolveAll() {
        getBossBarNamespace(world).apply {
            Bukkit.getBossBar(this)?.removeAll()
            if (!Bukkit.removeBossBar(this)) BloodNight.logger().config("Could not remove boss bar $key")
        }
        playerConsistencyMap.forEach {it.value.revert(Bukkit.getOfflinePlayer(it.key))}
    }

    class PlayerSound(val player: Player) : Comparable<PlayerSound> {

        private var next: Instant

        fun isNext() =
            next.isBefore(Instant.now())

        fun scheduleNext(seconds: Int) {
            next = next.plus(seconds.toLong(), ChronoUnit.SECONDS)
        }

        override fun compareTo(other: PlayerSound): Int {
            if (other.next === next) return 0
            return if (other.next.isAfter(next)) 1 else -1
        }

        init {
            next = Instant.now().plus(10, ChronoUnit.SECONDS)
        }
    }
}