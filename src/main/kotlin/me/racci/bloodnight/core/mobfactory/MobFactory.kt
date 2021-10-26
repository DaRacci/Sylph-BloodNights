package me.racci.bloodnight.core.mobfactory

import de.eldoria.eldoutilities.localization.ILocalizer
import de.eldoria.eldoutilities.utils.AttributeUtil
import me.racci.bloodnight.config.worldsettings.mobsettings.MobSetting
import me.racci.bloodnight.config.worldsettings.mobsettings.MobSettings
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.specialmobs.SpecialMob
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import java.util.function.Function

class MobFactory(
    entityType: EntityType,
    clazz: Class<out SpecialMob<*>>,
    factory: Function<LivingEntity, SpecialMob<*>>
) {
    private val factory: Function<LivingEntity, SpecialMob<*>>
    val mobName: String
    val entityType: EntityType

    fun wrap(entity: LivingEntity, mobSettings: MobSettings, mobSetting: MobSetting): SpecialMob<*> {
        SpecialMobUtil.tagSpecialMob(entity)
        applySettings(entity, mobSettings, mobSetting)
        return factory.apply(entity)
    }

    private fun applySettings(entity: LivingEntity, mobSettings: MobSettings, mobSetting: MobSetting) {
        val damage: AttributeInstance = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!
        AttributeUtil.setAttributeValue(
            entity,
            damage.attribute,
            mobSetting.applyDamage(damage.value, mobSettings.damageMultiplier).coerceAtMost(2048.0)
        )
        val health: AttributeInstance = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!
        AttributeUtil.setAttributeValue(
            entity,
            health.attribute,
            mobSetting.applyHealth(health.value, mobSettings.healthModifier).coerceAtMost(2048.0)
        )
        SpecialMobUtil.setSpecialMobType(entity, mobSetting.mobName)
        entity.health = health.value
        var displayName = mobSetting.displayName ?: ""
        if (displayName.trim { it <= ' ' }.isEmpty()) {
            displayName =
                ILocalizer.getPluginLocalizer(BloodNight::class.java).getMessage("mob." + mobSetting.mobName)
        }
        entity.customName = displayName
        entity.isCustomNameVisible = mobSettings.displayMobNames
    }

    init {
        this.entityType = entityType
        this.factory = factory
        mobName = clazz.simpleName
    }
}