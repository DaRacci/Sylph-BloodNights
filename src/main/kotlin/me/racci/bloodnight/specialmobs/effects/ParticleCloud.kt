package me.racci.bloodnight.specialmobs.effects

import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.entity.AreaEffectCloud
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType

open class ParticleCloud(effectCloud: AreaEffectCloud) {

    private val effectCloud: AreaEffectCloud

    fun tick() {
        effectCloud.duration = 60 * 20
    }

    open class Builder(entity: AreaEffectCloud) {

        protected val entity: AreaEffectCloud

        open fun ofColor(color: Color): Builder {
            entity.color = color
            return this
        }

        open fun withRadius(radius: Float): Builder {
            entity.radius = radius
            return this
        }

        open fun withParticle(particle: Particle): Builder {
            entity.particle = particle
            return this
        }

        open fun <T> withParticle(particle: Particle, data: T): Builder {
            entity.setParticle<T>(particle, data)
            return this
        }

        open fun build(): ParticleCloud {
            return ParticleCloud(entity)
        }

        init {
            this.entity = entity
            entity.radiusPerTick = 0f
            entity.radiusOnUse = 0f
            entity.durationOnUse = 0
            entity.duration = 60 * 20
            entity.color = Color.WHITE
            entity.particle = Particle.SPELL
            entity.reapplicationDelay = 20
            entity.durationOnUse = 0
            entity.radiusOnUse = 0f
            entity.radius = 3f
        }
    }

    companion object {
        fun builder(targetEntity: Entity): Builder {
            val loc = targetEntity.location
            val entity: AreaEffectCloud = loc.world.spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD) as AreaEffectCloud
            targetEntity.addPassenger(entity)
            return Builder(entity)
        }
    }

    init {
        this.effectCloud = effectCloud
    }
}