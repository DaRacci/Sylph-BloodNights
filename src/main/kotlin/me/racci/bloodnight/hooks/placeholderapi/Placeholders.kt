package me.racci.bloodnight.hooks.placeholderapi

import com.google.common.cache.CacheBuilder
import de.eldoria.eldoutilities.utils.Parser
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.racci.bloodnight.core.BloodNight
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.regex.Matcher
import java.util.regex.Pattern

class Placeholders : PlaceholderExpansion() {

    private val probability = Pattern.compile("probability(?:_(?<offset>[0-9]))?")
    private val worldActive = Pattern.compile("active_(?<world>.+)")
    private val worldCache  = CacheBuilder.newBuilder()
        .expireAfterWrite(500, TimeUnit.MILLISECONDS)
        .build<String, String>()

    override fun getAuthor()        = BloodNight.instance.description.authors.joinToString(", ")
    override fun getVersion()       = BloodNight.instance.description.version
    override fun getIdentifier()    = "bloodnight"

    override fun persist() = true
    override fun canRegister() = true

    override fun onPlaceholderRequest(player: Player?, params: String): String {
        if (player == null) return retriveFromWorldCache(params) { calcWorldPlaceholder(params) }
        val world: World = player.world
        return retriveFromWorldCache(
            world.name + "_" + params
        ) { calcPlayerPlaceholder(params, world) }
    }

    fun retriveFromWorldCache(key: String, calc: Callable<String>): String {
        try {
            return worldCache[key, calc]
        } catch (e: ExecutionException) {
            BloodNight.logger().log(Level.WARNING, "Could not calc placeholder settings for $key", e)
        }
        return ""
    }

    private fun calcWorldPlaceholder(params: String): String {
        val matcher = worldActive.matcher(params)
        return if (matcher.find()) worldActiveByString(matcher) else ""
    }

    private fun calcPlayerPlaceholder(params: String, world: World): String {
        var matcher = worldActive.matcher(params)
        if (matcher.find()) return worldActiveByString(matcher)
        matcher = probability.matcher(params)
        if (matcher.matches()) return probability(world, matcher)
        if ("seconds_left".equals(params, ignoreCase = true)) return secondsLeft(world)
        if ("percent_left".equals(params, ignoreCase = true)) return percentLeft(world)
        if ("active".equals(params, ignoreCase = true)) {
            return active(world)
        }
        BloodNight.logger().info("Could not calc placeholder settings for bloodnight_$params. No placeholder exists.")
        return ""
    }

    private fun worldActiveByString(matcher: Matcher): String {
        val worldName = matcher.group("world")
        val targetWorld: World = Bukkit.getWorld(worldName) ?: return "Invalid world"
        return active(targetWorld)
    }

    private fun active(world: World): String {
        return java.lang.String.valueOf(BloodNight.bloodNightAPI.isBloodNightActive(world))
    }

    private fun probability(world: World, matcher: Matcher): String {
        val offsetGroup = Optional.ofNullable(matcher.group("offset")).orElse("1")
        val offset = Parser.parseInt(offsetGroup).orElse(1)
        return java.lang.String.valueOf(BloodNight.bloodNightAPI.nextProbability(world, offset))
    }

    private fun percentLeft(world: World): String {
        return if (!BloodNight.bloodNightAPI.isBloodNightActive(world)) "0" else java.lang.String.format(
            "%.1f",
            BloodNight.bloodNightAPI.getPercentLeft(world)
        )
    }

    private fun secondsLeft(world: World): String {
        if (!BloodNight.bloodNightAPI.isBloodNightActive(world)) return "0:00"
        val seconds: Int = BloodNight.bloodNightAPI.getSecondsLeft(world)
        return if (seconds > 3600) {
            String.format("%d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60)
        } else String.format("%02d:%02d", seconds % 3600 / 60, seconds % 60)
    }
}