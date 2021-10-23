package me.racci.bloodnight.config.worldsettings.sound

import de.eldoria.bloodnight.core.BloodNight
import org.bukkit.Location
import org.bukkit.SoundCategory
import java.util.ArrayList

@SerializableAs("bloodNightSoundSetting")
class SoundSettings : ConfigurationSerializable {
    private var minInterval = 10
    private var maxInterval = 40

    @Getter
    private var channel: SoundCategory? = SoundCategory.AMBIENT
    private var startSounds: List<SoundEntry> = object : ArrayList<SoundEntry?>() {
        init {
            for (sound in Sounds.START) {
                add(SoundEntry(sound, arrayOf(0.8, 1.0), arrayOf(1.0)))
            }
        }
    }
    private var endSounds: List<SoundEntry> = object : ArrayList<SoundEntry?>() {
        init {
            for (sound in Sounds.START) {
                add(SoundEntry(sound, arrayOf(0.8, 1.0), arrayOf(1.0)))
            }
        }
    }
    private var randomSounds: List<SoundEntry> = object : ArrayList<SoundEntry?>() {
        init {
            for (sound in Sounds.SPOOKY) {
                add(SoundEntry(sound, arrayOf(0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6), arrayOf(0.2, 0.4, 0.6, 0.8, 1.0)))
            }
        }
    }

    constructor(objectMap: Map<String?, Any?>?) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        minInterval = map.getValueOrDefault<Int>("minInterval", minInterval)
        maxInterval = map.getValueOrDefault<Int>("maxInterval", maxInterval)
        val channel: String = map.getValueOrDefault<String>("channel", channel!!.name)
        this.channel = EnumUtil.parse<SoundCategory>(channel, SoundCategory::class.java)
        if (this.channel == null) {
            this.channel = SoundCategory.AMBIENT
            BloodNight.logger().warning("Channel $channel is invalid. Changed to AMBIENT.")
        }
        startSounds = map.getValueOrDefault<List<SoundEntry>>("startSounds", startSounds)
        endSounds = map.getValueOrDefault<List<SoundEntry>>("endSounds", endSounds)
        randomSounds = map.getValueOrDefault<List<SoundEntry>>("randomSounds", randomSounds)
    }

    constructor() {}

    fun playRandomSound(player: Player, location: Location?) {
        if (randomSounds.isEmpty()) return
        randomSounds[ThreadLocalRandom.current().nextInt(randomSounds.size)].play(player, location, channel)
    }

    fun playStartSound(player: Player) {
        playSounds(player, startSounds)
    }

    fun playEndsound(player: Player) {
        playSounds(player, endSounds)
    }

    private fun playSounds(player: Player, sounds: Collection<SoundEntry>) {
        for (sound in sounds) {
            sound.play(player, player.getLocation(), channel)
        }
    }

    val waitSeconds: Int
        get() = ThreadLocalRandom.current().nextInt(minInterval, maxInterval + 1)

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("minInterval", minInterval)
            .add("maxInterval", maxInterval)
            .add("channel", channel!!.name)
            .add("startSounds", startSounds)
            .add("endSounds", endSounds)
            .add("randomSounds", randomSounds)
            .build()
    }
}