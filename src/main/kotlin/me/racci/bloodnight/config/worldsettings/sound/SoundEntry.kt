package me.racci.bloodnight.config.worldsettings.sound

import de.eldoria.bloodnight.core.BloodNight
import de.eldoria.eldoutilities.serialization.SerializationUtil
import de.eldoria.eldoutilities.serialization.TypeResolvingMap
import de.eldoria.eldoutilities.utils.EMath
import de.eldoria.eldoutilities.utils.EnumUtil
import me.racci.bloodnight.core.BloodNight
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ThreadLocalRandom

@SerializableAs("bloodNightSoundEntry")
class SoundEntry : ConfigurationSerializable {
    private var sound: Sound? = Sound.UI_BUTTON_CLICK
    private var pitch: MutableList<Double> = object : ArrayList<Double>() {
        init {
            add(1.0)
        }
    }
    private var volume: MutableList<Double> = object : ArrayList<Double>() {
        init {
            add(1.0)
        }
    }

    constructor(objectMap: Map<String, Any>) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        val name: String = map.getValueOrDefault("sound", sound!!.name)
        sound = EnumUtil.parse(name, Sound::class.java)
        if (sound == null) {
            sound = Sound.UI_BUTTON_CLICK
            BloodNight.logger().warning("§4Sound " + name + " is not a valid sound. Changed to " + sound!!.name)
        }
        pitch = map.getValueOrDefault<List<Double>>("pitch", pitch)
        clampArray(pitch, 0.01, 2.0)
        volume = map.getValueOrDefault<List<Double>>("volume", volume)
        clampArray(volume, 0.01, 1.0)
    }

    constructor(sound: Sound?, pitch: Array<Double>, volume: Array<Double>) {
        this.sound = sound
        this.pitch = Arrays.asList(*pitch)
        this.volume = Arrays.asList(*volume)
    }

    private fun clampArray(values: MutableList<Double>, min: Double, max: Double) {
        for (i in values.indices) {
            values[i] = EMath.clamp(min, max, values[i])
        }
    }

    fun play(player: Player, location: Location?, channel: SoundCategory?) {
        player.playSound(location, sound, channel, getPitch().toFloat(), getVolume().toFloat())
    }

    private fun getPitch(): Double {
        return if (pitch.isEmpty()) 1 else pitch[ThreadLocalRandom.current().nextInt(pitch.size)]
    }

    private fun getVolume(): Double {
        return if (volume.isEmpty()) 1 else volume[ThreadLocalRandom.current().nextInt(volume.size)]
    }

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("sound", sound!!.name)
            .add("pitch", pitch)
            .add("volume", volume)
            .build()
    }
}