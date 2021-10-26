package me.racci.bloodnight.config.worldsettings

import de.eldoria.eldoutilities.serialization.SerializationUtil
import de.eldoria.eldoutilities.serialization.TypeResolvingMap
import me.racci.bloodnight.core.manager.nightmanager.util.NightUtil
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.util.concurrent.ThreadLocalRandom

@SerializableAs("bloodNightNightSettings")
class NightSettings : ConfigurationSerializable {
    /**
     * If false a blood night can not be skipped by sleeping in a bed.
     */
    var skippable = false

    /**
     * Tick when a night starts to be a night.
     */
    var nightBegin = 14000

    /**
     * Tick when a night stops to be a night.
     */
    var nightEnd = 23000
    var startCommands: List<String> = ArrayList()
    var endCommands: List<String> = ArrayList()
    var nightDurationMode = NightDuration.NORMAL

    /**
     * The duration of a night when [.nightDurationMode] is set to [NightDuration.EXTENDED].
     *
     *
     * The min duration of a night when when [.nightDurationMode] is set to [NightDuration.RANGE].
     */
    var nightDuration = 600
        set(nightDuration) {
            field = nightDuration
            maxNightDuration = field.coerceAtLeast(maxNightDuration)
        }

    /**
     * The max duration of a night when [.nightDurationMode] is set to [NightDuration.RANGE]
     */
    var maxNightDuration = 600
        set(maxNightDuration) {
            field = nightDuration.coerceAtLeast(maxNightDuration)
        }

    /**
     * The duration of the current night in ticks.
     */
    @Transient
    var currentNightDuration = 0 ;  private set

    constructor()

    constructor(objectMap: Map<String, Any>) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        skippable = map.getValueOrDefault("skippable", skippable)
        nightBegin = map.getValueOrDefault("nightBegin", nightBegin)
        nightEnd = map.getValueOrDefault("nightEnd", nightEnd)
        startCommands = map.getValueOrDefault("startCommands", startCommands)
        endCommands = map.getValueOrDefault("endCommands", endCommands)
        nightDurationMode = if (objectMap.containsKey("overrideNightDuration")) {
            if (map.getValue("overrideNightDuration")) NightDuration.EXTENDED else NightDuration.NORMAL
        } else {
            map.getValueOrDefault("nightDurationMode", nightDurationMode, NightDuration::class.java)
        }
        nightDuration = map.getValueOrDefault("nightDuration", nightDuration)
        maxNightDuration = map.getValueOrDefault("maxNightDuration", maxNightDuration)
    }

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("skippable", skippable)
            .add("nightBegin", nightBegin)
            .add("nightEnd", nightEnd)
            .add("startCommands", startCommands)
            .add("endCommands", endCommands)
            .add("nightDurationMode", nightDurationMode.name)
            .add("nightDuration", nightDuration)
            .build()
    }

    fun regenerateNightDuration() {
        currentNightDuration = when (nightDurationMode) {
            NightDuration.NORMAL -> NightUtil.getDiff(nightBegin.toLong(), nightEnd.toLong()).toInt()
            NightDuration.EXTENDED -> nightDuration * 20
            NightDuration.RANGE -> ThreadLocalRandom.current().nextInt(nightDuration, maxNightDuration + 1) * 20
        }
    }

    val isCustomNightDuration: Boolean
        get() = nightDurationMode != NightDuration.NORMAL

    enum class NightDuration {
        NORMAL, EXTENDED, RANGE
    }
}