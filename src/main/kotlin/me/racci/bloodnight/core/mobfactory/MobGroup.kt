package me.racci.bloodnight.core.mobfactory

import me.racci.bloodnight.specialmobs.SpecialMob
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import java.util.function.Function

class MobGroup(entityType: EntityType) {

    val entityType: EntityType
    val factories: ArrayList<MobFactory> = ArrayList()

    val baseClass: Class<out Entity>
        get() = entityType.entityClass!!

    fun registerFactory(factory: MobFactory) {
        factories.add(factory)
    }

    fun registerFactory(clazz: Class<out SpecialMob<*>>, factory: Function<LivingEntity, SpecialMob<*>>) {
        factories.add(MobFactory(entityType, clazz, factory))
    }

    init {
        this.entityType = entityType
    }
}