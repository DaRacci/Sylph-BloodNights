package me.racci.bloodnight.config.worldsettings

import com.google.common.collect.Lists
import de.eldoria.eldoutilities.serialization.SerializationUtil
import de.eldoria.eldoutilities.serialization.TypeResolvingMap
import de.eldoria.eldoutilities.utils.EnumUtil
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.util.*

@SerializableAs("bloodNightBossBarSettings")
class BossBarSettings : ConfigurationSerializable {
    var enabled = true

    /**
     * Boss bar title with § as color identifier
     */
    var title = "§c§lBlood Night"
        set(title) {
            field = title.replace("&", "§")
        }
    var color: BarColor = BarColor.RED
    var effects: MutableList<BarFlag> = object : ArrayList<BarFlag>() {
        init {
            add(BarFlag.CREATE_FOG)
            add(BarFlag.DARKEN_SKY)
        }
    }

    constructor()

    constructor(objectMap: Map<String, Any>) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        enabled = map.getValue("enabled")
        title = map.getValue("title")
        color = map.getValue(
            "color"
        ) {EnumUtil.parse(it, BarColor::class.java) }
        val effects: List<String> = map.getValue("effects")
        this.effects = effects.mapNotNull{EnumUtil.parse(it, BarFlag::class.java)}.toMutableList()
    }

    fun toggleEffect(flag: BarFlag) {
        if (effects.contains(flag)) {
            effects.remove(flag)
        } else {
            effects.add(flag)
        }
    }

    fun getEffects() =
        effects.toTypedArray()

    fun isEffectEnabled(flag: BarFlag) =
        effects.contains(flag)

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("enabled", enabled)
            .add("title", title)
            .add("color", color)
            .add("effects", Lists.newArrayList(effects).map(BarFlag::toString))
            .build()
    }
}