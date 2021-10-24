package me.racci.bloodnight.config.worldsettings.sound

import de.eldoria.eldoutilities.serialization.SerializationUtil
import de.eldoria.eldoutilities.serialization.TypeResolvingMap
import de.eldoria.eldoutilities.utils.EMath
import de.eldoria.eldoutilities.utils.EnumUtil
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.entity.Player
import java.util.concurrent.ThreadLocalRandom

@SerializableAs("bloodNightSoundEntry")
class SoundEntry : ConfigurationSerializable {
    var sound: Sound = Sound.UI_BUTTON_CLICK
    var pitch: ArrayList<Double> = object : ArrayList<Double>() {
        init {
            add(1.0)
        }
    }
    var volume: ArrayList<Double> = object : ArrayList<Double>() {
        init {
            add(1.0)
        }
    }

    constructor(objectMap: Map<String, Any>) {
        val map     = SerializationUtil.mapOf(objectMap)
        val name    = map.getValueOrDefault("sound", sound.name)
        sound       = EnumUtil.parse(name, Sound::class.java) ?: Sound.UI_BUTTON_CLICK
        pitch       = map.getValueOrDefault("pitch", pitch)
        clampArray(pitch, 0.01, 2.0)
        volume      = map.getValueOrDefault("volume", volume)
        clampArray(volume, 0.01, 1.0)
    }

    constructor(sound: Sound, pitch: Array<Double>, volume: Array<Double>) {
        this.sound = sound
        this.pitch = arrayListOf(*pitch)
        this.volume = arrayListOf(*volume)
    }

    private fun clampArray(values: MutableList<Double>, min: Double, max: Double) {
        for (i in values.indices) {
            values[i] = EMath.clamp(min, max, values[i])
        }
    }

    fun play(player: Player, location: Location, channel: SoundCategory) {
        player.playSound(location, sound, channel, getPitch().toFloat(), getVolume().toFloat())
    }

    private fun getPitch() =
        if (pitch.isEmpty()) 1.0 else pitch[ThreadLocalRandom.current().nextInt(pitch.size)]

    private fun getVolume() =
        if (volume.isEmpty()) 1.0 else volume[ThreadLocalRandom.current().nextInt(volume.size)]

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("sound", sound.name)
            .add("pitch", pitch)
            .add("volume", volume)
            .build()
    }
}