package me.racci.bloodnight.config.worldsettings.mobsettings

import com.google.common.base.Objects
import lombok.Getter
import java.util.ArrayList

@Getter
@SerializableAs("bloodNightMobSetting")
class MobSetting : ConfigurationSerializable {
    /**
     * plugin name of the mob
     */
    private val mobName: String?

    /**
     * The display name of the mob. Uses § as color code identifier.
     */
    private var displayName: String? = null

    /**
     * Indicates if this mob can be spawned
     */
    @Setter
    private var active = true

    /**
     * Amount of drops.
     */
    @Setter
    private var dropAmount = -1

    /**
     * If this is true only drops from mobs are choosen and default drops will not drop. if false the drops will be
     * added to default drops.
     */
    @Setter
    private var overrideDefaultDrops = false

    @Setter
    private var drops: List<Drop> = ArrayList()

    @Setter
    private var healthModifier = MobValueModifier.DEFAULT

    /**
     * The max health of a mob. -1 is disabled
     */
    @Setter
    private var health = 2.0

    @Setter
    private var damageModifier = MobValueModifier.DEFAULT

    /**
     * The damage a mob makes. -1 is disabled
     */
    @Setter
    private var damage = 2.0

    constructor(objectMap: Map<String?, Any?>?) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        mobName = map.getValue<String>("mobName")
        if (mobName == null) {
            throw NullPointerException("Mob name is null. This is not allowed")
        }
        setDisplayName(map.getValueOrDefault<String>("displayName", ""))
        active = map.getValueOrDefault<Boolean>("active", active)
        dropAmount = map.getValueOrDefault<Int>("dropAmount", dropAmount)
        overrideDefaultDrops = map.getValueOrDefault<Boolean>("overrideDefaultDrops", overrideDefaultDrops)
        drops = map.getValueOrDefault<List<Drop>>("drops", drops)
        healthModifier = EnumUtil.parse<MobValueModifier>(
            map.getValueOrDefault<String>("healthModifier", healthModifier.name),
            MobValueModifier::class.java
        )
        health = map.getValueOrDefault<Double>("health", health)
        damageModifier = EnumUtil.parse<MobValueModifier>(
            map.getValueOrDefault<String>("damageModifier", damageModifier.name),
            MobValueModifier::class.java
        )
        damage = map.getValueOrDefault<Double>("damage", damage)
    }

    constructor(mobName: String?) {
        this.mobName = mobName
        displayName = ""
    }

    fun getOverridenDropAmount(dropAmount: Int): Int {
        return if (this.dropAmount <= 0) dropAmount else this.dropAmount
    }

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

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as MobSetting
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
            else -> throw IllegalStateException("Unexpected value: $damageModifier")
        }
    }

    fun applyHealth(baseValue: Double, defaultMultiplier: Double): Double {
        return when (healthModifier) {
            MobValueModifier.DEFAULT -> baseValue * defaultMultiplier
            MobValueModifier.MULTIPLY -> baseValue * health
            MobValueModifier.VALUE -> health
            else -> throw IllegalStateException("Unexpected value: $healthModifier")
        }
    }

    /**
     * Sets the display name.
     *
     *
     * This will replace &amp; with §
     *
     * @param displayName display name to set.
     */
    fun setDisplayName(displayName: String) {
        this.displayName = displayName.replace("&", "§")
    }
}