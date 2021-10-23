package me.racci.bloodnight.config.worldsettings.deathactions

import lombok.Getter
import java.util.function.Function

@Getter
@SerializableAs("bloodNightPotionEffectSettings")
class PotionEffectSettings : ConfigurationSerializable {
    private var effectType: PotionEffectType
    private var duration = 10

    constructor(effectType: PotionEffectType, duration: Int) {
        this.effectType = effectType
        this.duration = duration
    }

    constructor(objectMap: Map<String?, Any?>?) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        effectType = map.getValue<PotionEffectType>(
            "effectType",
            Function<String, PotionEffectType> { name: String? -> PotionEffectType.getByName(name) })
        duration = map.getValueOrDefault<Int>("duration", duration)
    }

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("effectType", effectType.getName())
            .add("duration", duration)
            .build()
    }
}