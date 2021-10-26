package me.racci.bloodnight.config.worldsettings.sound

import de.eldoria.eldoutilities.serialization.SerializationUtil
import de.eldoria.eldoutilities.serialization.TypeResolvingMap
import de.eldoria.eldoutilities.utils.EnumUtil
import me.racci.bloodnight.util.Sounds
import org.bukkit.Location
import org.bukkit.SoundCategory
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.entity.Player
import java.util.concurrent.ThreadLocalRandom

@SerializableAs("bloodNightSoundSetting")
class SoundSettings : ConfigurationSerializable {
    private var minInterval = 10
    private var maxInterval = 40

    private var channel: SoundCategory = SoundCategory.AMBIENT;
    private var startSounds: ArrayList<SoundEntry> = object : ArrayList<SoundEntry>() {
        init {
            for (sound in Sounds.START) {
                add(SoundEntry(sound, arrayOf(0.8, 1.0), arrayOf(1.0)))
            }
        }
    }
    private var endSounds: ArrayList<SoundEntry> = object : ArrayList<SoundEntry>() {
        init {
            for (sound in Sounds.START) {
                add(SoundEntry(sound, arrayOf(0.8, 1.0), arrayOf(1.0)))
            }
        }
    }
    private var randomSounds: ArrayList<SoundEntry> = object : ArrayList<SoundEntry>() {
        init {
            for (sound in Sounds.SPOOKY) {
                add(SoundEntry(sound, arrayOf(0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6), arrayOf(0.2, 0.4, 0.6, 0.8, 1.0)))
            }
        }
    }

    constructor(objectMap: Map<String, Any>) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        minInterval = map.getValueOrDefault("minInterval", minInterval)
        maxInterval = map.getValueOrDefault("maxInterval", maxInterval)
        val channel = map.getValueOrDefault("channel", channel.name)
        this.channel = EnumUtil.parse(channel, SoundCategory::class.java) ?: SoundCategory.AMBIENT
        startSounds = map.getValueOrDefault("startSounds", startSounds)
        endSounds = map.getValueOrDefault("endSounds", endSounds)
        randomSounds = map.getValueOrDefault("randomSounds", randomSounds)
    }

    constructor()

    fun playRandomSound(player: Player, location: Location) {
        if (randomSounds.isEmpty()) return
        randomSounds[ThreadLocalRandom.current().nextInt(randomSounds.size)].play(player, location, channel)
    }

    fun playStartSound(player: Player) {
        playSounds(player, startSounds)
    }

    fun playEndSound(player: Player) {
        playSounds(player, endSounds)
    }

    private fun playSounds(player: Player, sounds: Collection<SoundEntry>) {
        for (sound in sounds) {
            sound.play(player, player.location, channel)
        }
    }

    val waitSeconds: Int
        get() = ThreadLocalRandom.current().nextInt(minInterval, maxInterval + 1)

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("minInterval", minInterval)
            .add("maxInterval", maxInterval)
            .add("channel", channel.name)
            .add("startSounds", startSounds)
            .add("endSounds", endSounds)
            .add("randomSounds", randomSounds)
            .build()
    }
}