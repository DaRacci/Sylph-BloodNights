package me.racci.bloodnight.config.worldsettings.deathactions

import lombok.Getter
import java.util.ArrayList
import java.util.function.BiFunction

@Getter
@Setter
@SerializableAs("bloodNightPlayerDeathActions")
class PlayerDeathActions : DeathActions {
    var respawnEffects: Map<PotionEffectType, PotionEffectSettings> =
        object : HashMap<PotionEffectType?, PotionEffectSettings?>() {
            init {
                put(PotionEffectType.CONFUSION, PotionEffectSettings(PotionEffectType.CONFUSION, 5))
            }
        }

    /**
     * Commands which will be executed when a player dies.
     *
     *
     * Should support the `{player}` placeholder.
     */
    private var deathCommands: List<String> = ArrayList()

    /**
     * Probability of the player to lose and not drop its inventory.
     */
    private var loseInvProbability = 0
    private var loseExpProbability = 0

    constructor(objectMap: Map<String?, Any?>?) : super(objectMap) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        deathCommands = map.getValueOrDefault<List<String>>("deathCommands", deathCommands)
        loseInvProbability = map.getValueOrDefault<Int>("loseInvProbability", loseInvProbability)
        loseExpProbability = map.getValueOrDefault<Int>("loseExpProbability", loseExpProbability)
        respawnEffects = map.getMap<PotionEffectType, PotionEffectSettings>(
            "respawnEffects",
            BiFunction<String, PotionEffectSettings, PotionEffectType> { key: String?, potionEffectSettings: PotionEffectSettings? ->
                PotionEffectType.getByName(
                    key
                )
            })
    }

    constructor() {}

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder(super.serialize())
            .add("deathCommands", deathCommands)
            .add("loseInvProbability", loseInvProbability)
            .add("loseExpProbability", loseExpProbability)
            .addMap<PotionEffectType, PotionEffectSettings>("respawnEffects", respawnEffects,
                BiFunction<PotionEffectType, PotionEffectSettings, String> { potionEffectType: PotionEffectType, potionEffectSettings: PotionEffectSettings? -> potionEffectType.getName() })
            .build()
    }
}