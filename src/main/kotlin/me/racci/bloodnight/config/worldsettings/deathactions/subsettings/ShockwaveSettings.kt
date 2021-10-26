package me.racci.bloodnight.config.worldsettings.deathactions.subsettings

import de.eldoria.eldoutilities.serialization.SerializationUtil
import me.racci.bloodnight.config.worldsettings.deathactions.PotionEffectSettings
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import kotlin.math.pow

@SerializableAs("bloodNightShockwaveSettings")
class ShockwaveSettings : ConfigurationSerializable {
    /**
     * Probability that a shockwave is spawned at the death location.
     *
     *
     * A Shockwave will push every entity away from the death position.
     */
    var shockwaveProbability = 10

    /**
     * Power of shockwave. used to multiply the velocity vector.
     *
     *
     * Power will be less depending on the distance to shockwave center.
     */
    var shockwavePower = 10

    /**
     * Range where player should be affected by shockwave.
     */
    var shockwaveRange = 10

    /**
     * min duration of effects when on the edge of range
     */
    var minDuration = 0.1

    var shockwaveEffects: HashMap<PotionEffectType, PotionEffectSettings> =
        object : HashMap<PotionEffectType, PotionEffectSettings>() {
            init {
                put(PotionEffectType.CONFUSION, PotionEffectSettings(PotionEffectType.CONFUSION, 5))
            }
        }

    constructor(objectMap: Map<String, Any>) {
        val map = SerializationUtil.mapOf(objectMap)
        shockwaveProbability = map.getValueOrDefault("shockwaveProbability", shockwaveProbability)
        shockwavePower = map.getValueOrDefault("shockwavePower", shockwavePower)
        shockwaveRange = map.getValueOrDefault("shockwaveRange", shockwaveRange)
        shockwaveEffects = map.getMap<PotionEffectType, PotionEffectSettings>("shockwaveEffects") { it, _ ->
            PotionEffectType.getByName(it)
        } as HashMap
        minDuration = map.getValueOrDefault("minDuration", minDuration)
    }

    constructor()

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("shockwaveProbability", shockwaveProbability)
            .add("shockwavePower", shockwavePower)
            .add("shockwaveRange", shockwaveRange)
            .addMap("shockwaveEffects", shockwaveEffects) { it, _ -> it.name }
            .add("minDuration", minDuration)
            .build()
    }

    fun getPower(vector: Vector): Double {
        val range = shockwaveRange.toDouble().pow(2.0)
        val dist = vector.lengthSquared()
        return if (dist >= range) 0.0 else (1 - dist / range) * (shockwavePower / 10.0)
    }

    fun applyEffects(entity: Entity, power: Double) {
        if (entity !is LivingEntity) return
        val livingEntity: LivingEntity = entity
        for (potionEffectType in shockwaveEffects.values) {
            val percent = power / (shockwavePower / 10.0)
            val duration = minDuration.coerceAtLeast(potionEffectType.duration * percent) * 20
            livingEntity.addPotionEffect(
                PotionEffect(
                    potionEffectType.effectType,
                    duration.toInt(),
                    1,
                    false,
                    true
                )
            )
        }
    }
}