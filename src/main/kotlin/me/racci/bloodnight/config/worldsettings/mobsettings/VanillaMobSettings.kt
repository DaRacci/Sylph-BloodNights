package me.racci.bloodnight.config.worldsettings.mobsettings

import lombok.Getter

@Setter
@Getter
@SerializableAs("bloodNightVanillaMobSettings")
class VanillaMobSettings : ConfigurationSerializable {
    /**
     * The modifier which will be multiplied with monster damage when a non special mob deals damage to players.
     */
    private var damageMultiplier = 2.0

    /**
     * The value the damage will be divided by when a player damages a non special mob
     */
    private var healthMultiplier = 2.0

    /**
     * The modifier which will be multiplied with the dropped item amount.
     */
    private var dropMultiplier = 2.0
    private var vanillaDropMode = VanillaDropMode.VANILLA
    private var extraDrops = 1

    constructor() {}
    constructor(objectMap: Map<String?, Any?>?) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        damageMultiplier = map.getValueOrDefault<Double>("damageMultiplier", damageMultiplier)
        healthMultiplier = map.getValueOrDefault<Double>("healthMultiplier", healthMultiplier)
        dropMultiplier = map.getValueOrDefault<Double>("dropMultiplier", dropMultiplier)
        vanillaDropMode = EnumUtil.parse<VanillaDropMode>(
            map.getValueOrDefault<String>("vanillaDropMode", vanillaDropMode.name),
            VanillaDropMode::class.java
        )
        extraDrops = map.getValueOrDefault<Int>("extraDrops", extraDrops)
    }

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("damageMultiplier", damageMultiplier)
            .add("healthMultiplier", healthMultiplier)
            .add("dropMultiplier", dropMultiplier)
            .add("vanillaDropMode", vanillaDropMode.toString())
            .add("extraDrops", extraDrops)
            .build()
    }
}