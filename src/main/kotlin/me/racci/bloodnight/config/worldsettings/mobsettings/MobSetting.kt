package me.racci.bloodnight.config.worldsettings.mobsettings

import com.google.common.base.Objects
import de.eldoria.eldoutilities.serialization.SerializationUtil
import de.eldoria.eldoutilities.serialization.TypeResolvingMap
import de.eldoria.eldoutilities.utils.EnumUtil
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.util.ArrayList

@SerializableAs("bloodNightMobSetting")
class MobSetting : ConfigurationSerializable {
    /**
     * plugin name of the mob
     */
    val mobName: String

    /**
     * The display name of the mob. Uses § as color code identifier.
     */
    var displayName: String? = null
        set(displayName) {
            field = displayName?.replace("&", "§")
        }

    /**
     * Indicates if this mob can be spawned
     */
    var active = true

    /**
     * Amount of drops.
     */
    var dropAmount = -1

    /**
     * If this is true only drops from mobs are chosen and default drops will not drop. if false the drops will be
     * added to default drops.
     */
    var overrideDefaultDrops = false

    var drops: List<Drop> = ArrayList()

    var healthModifier = MobValueModifier.DEFAULT

    /**
    * The max health of a mob. -1 is disabled
    */
    var health = 2.0

    var damageModifier = MobValueModifier.DEFAULT

    /**
     * The damage a mob makes. -1 is disabled
     */
    var damage = 2.0

    constructor(objectMap: Map<String, Any>) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        mobName                 = map.getValue("mobName")
        displayName             = map.getValueOrDefault("displayName", "")
        active                  = map.getValueOrDefault("active", active)
        dropAmount              = map.getValueOrDefault("dropAmount", dropAmount)
        overrideDefaultDrops    = map.getValueOrDefault("overrideDefaultDrops", overrideDefaultDrops)
        drops                   = map.getValueOrDefault("drops", drops)
        healthModifier          = EnumUtil.parse(
            map.getValueOrDefault("healthModifier", healthModifier.name),
            MobValueModifier::class.java
        )
        health                  = map.getValueOrDefault("health", health)
        damageModifier          = EnumUtil.parse(
            map.getValueOrDefault("damageModifier", damageModifier.name),
            MobValueModifier::class.java
        )
        damage                  = map.getValueOrDefault("damage", damage)
    }

    constructor(mobName: String) {
        this.mobName = mobName
        displayName = ""
    }

    fun getOverriddenDropAmount(dropAmount: Int) =
        if (this.dropAmount <= 0) dropAmount else this.dropAmount

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("mobName", mobName)
            .add("displayName", displayName)
            .add("active", active)
            .add("dropAmount", dropAmount)
            .add("overrideDefaultDrops", overrideDefaultDrops)
            .add("drops", drops)
            .add("healthModifier", healthModifier.name)
            .add("health", health)
            .add("damageModifier", damageModifier.name)
            .add("damage", damage)
            .build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as MobSetting
        return mobName.equals(that.mobName, ignoreCase = true)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(mobName)
    }

    fun applyDamage(baseValue: Double, defaultMultiplier: Double): Double {
        return when (damageModifier) {
            MobValueModifier.DEFAULT -> baseValue * defaultMultiplier
            MobValueModifier.MULTIPLY -> baseValue * damage
            MobValueModifier.VALUE -> damage
        }
    }

    fun applyHealth(baseValue: Double, defaultMultiplier: Double): Double {
        return when (healthModifier) {
            MobValueModifier.DEFAULT -> baseValue * defaultMultiplier
            MobValueModifier.MULTIPLY -> baseValue * health
            MobValueModifier.VALUE -> health
        }
    }
}