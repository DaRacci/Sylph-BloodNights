package me.racci.bloodnight.config.worldsettings.deathactions.subsettings

import de.eldoria.bloodnight.config.worldsettings.deathactions.PotionEffectSettings
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import java.util.function.BiFunction

@Getter
@Setter
@SerializableAs("bloodNightShockwaveSettings")
class ShockwaveSettings : ConfigurationSerializable {
    /**
     * Probability that a shockwave is spawned at the death location.
     *
     *
     * A Shockwave will push every entity away from the death position.
     */
    protected var shockwaveProbability = 10

    /**
     * Power of shockwave. used to multiply the velocity vector.
     *
     *
     * Power will be less depending on the distance to shockwave center.
     */
    protected var shockwavePower = 10

    /**
     * Range where player should be affected by shockwave.
     */
    protected var shockwaveRange = 10

    /**
     * min duration of effects when on the edge of range
     */
    protected var minDuration = 0.1
    private var shockwaveEffects: Map<PotionEffectType, PotionEffectSettings> =
        object : HashMap<PotionEffectType?, PotionEffectSettings?>() {
            init {
                put(PotionEffectType.CONFUSION, PotionEffectSettings(PotionEffectType.CONFUSION, 5))
            }
        }

    constructor(objectMap: Map<String?, Any?>?) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        shockwaveProbability = map.getValueOrDefault<Int>("shockwaveProbability", shockwaveProbability)
        shockwavePower = map.getValueOrDefault<Int>("shockwavePower", shockwavePower)
        shockwaveRange = map.getValueOrDefault<Int>("shockwaveRange", shockwaveRange)
        shockwaveEffects = map.getMap<PotionEffectType, PotionEffectSettings>(
            "shockwaveEffects",
            BiFunction<String, PotionEffectSettings, PotionEffectType> { key: String?, potionEffectSettings: PotionEffectSettings? ->
                PotionEffectType.getByName(
                    key
                )
            })
        minDuration = map.getValueOrDefault<Double>("minDuration", minDuration)
    }

    constructor() {}

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("shockwaveProbability", shockwaveProbability)
            .add("shockwavePower", shockwavePower)
            .add("shockwaveRange", shockwaveRange)
            .addMap("shockwaveEffects", shockwaveEffects,
                BiFunction<K, V, String> { potionEffectType: K, potionEffectSettings: V? -> potionEffectType.getName() })
            .add("minDuration", minDuration)
            .build()
    }

    fun getPower(vector: Vector): Double {
        val range = Math.pow(shockwaveRange.toDouble(), 2.0)
        val dist = vector.lengthSquared()
        return if (dist >= range) 0 else (1 - dist / range) * (shockwavePower / 10.0)
    }

    fun applyEffects(entity: Entity, power: Double) {
        if (entity !is LivingEntity) return
        val livingEntity: LivingEntity = entity as LivingEntity
        for (potionEffectType in shockwaveEffects.values) {
            if (potionEffectType.getEffectType() == null) continue
            val percent = power / (shockwavePower / 10.0)
            val duration = Math.max(minDuration, potionEffectType.getDuration() * percent) * 20
            livingEntity.addPotionEffect(
                PotionEffect(
                    potionEffectType.getEffectType(),
                    duration.toInt(),
                    1,
                    false,
                    true
                )
            )
        }
    }
}