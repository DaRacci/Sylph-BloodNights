package me.racci.bloodnight.core.manager.nightmanager.util

import org.bukkit.OfflinePlayer
import org.bukkit.Statistic
import org.bukkit.entity.Player

class ConsistencyCache(player: Player) {

    private val timeSinceRest = player.getStatistic(Statistic.TIME_SINCE_REST)

    fun revert(player: OfflinePlayer) {
        player.setStatistic(Statistic.TIME_SINCE_REST, timeSinceRest)
    }
}