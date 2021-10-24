package me.racci.bloodnight.config.worldsettings.deathactions

import de.eldoria.eldoutilities.serialization.SerializationUtil
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.potion.PotionEffectType


@SerializableAs("bloodNightPlayerDeathActions")
class PlayerDeathActions : DeathActions {
    var respawnEffects: HashMap<PotionEffectType, PotionEffectSettings> =
        object : HashMap<PotionEffectType, PotionEffectSettings>() {
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
    var deathCommands = ArrayList<String>()

    /**
     * Probability of the player to lose and not drop its inventory.
     */
    var loseInvProbability = 0
    var loseExpProbability = 0

    constructor(objectMap: Map<String, Any>) : super(objectMap) {
        val map             = SerializationUtil.mapOf(objectMap)
        deathCommands       = map.getValueOrDefault("deathCommands", deathCommands)
        loseInvProbability  = map.getValueOrDefault("loseInvProbability", loseInvProbability)
        loseExpProbability  = map.getValueOrDefault("loseExpProbability", loseExpProbability)
        respawnEffects      = map.getMap<PotionEffectType, PotionEffectSettings>("respawnEffects") {it,_->PotionEffectType.getByName(it)!!} as HashMap
    }

    constructor()

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder(super.serialize())
            .add("deathCommands", deathCommands)
            .add("loseInvProbability", loseInvProbability)
            .add("loseExpProbability", loseExpProbability)
            .addMap("respawnEffects", respawnEffects) {it,_->it.name}
            .build()
    }
}