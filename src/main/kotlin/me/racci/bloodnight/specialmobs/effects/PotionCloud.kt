package me.racci.bloodnight.specialmobs.effects

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.AreaEffectCloud
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.potion.PotionData
import org.bukkit.projectiles.ProjectileSource

class PotionCloud(effectCloud: AreaEffectCloud) : ParticleCloud(effectCloud)
{
    class Builder(entity: AreaEffectCloud) : ParticleCloud.Builder(entity) {

        fun setPotionType(potionType: PotionData): Builder {
            entity.basePotionData = potionType
            return this
        }

        /**
         * Set duration
         *
         * @param duration duration in seconds
         * @return builder with changed duration
         */
        fun setDuration(duration: Int): Builder {
            entity.duration = duration * 20
            return this
        }

        fun withReapplyDelay(delay: Int): Builder {
            entity.reapplicationDelay = delay
            return this
        }

        fun withDurationDecreaseOnApply(duration: Int): Builder {
            entity.durationOnUse = duration
            return this
        }

        fun withRadiusDecreaseOnApply(radius: Float): Builder {
            entity.radiusOnUse = radius
            return this
        }

        fun setRadiusPerTick(radius: Float): Builder {
            entity.radiusPerTick = radius
            return this
        }

        fun fromSource(source: ProjectileSource): Builder {
            entity.source = source
            return this
        }

        override fun ofColor(color: Color): Builder {
            super.ofColor(color)
            return this
        }

        override fun withRadius(radius: Float): Builder {
            super.withRadius(radius)
            return this
        }

        override fun withParticle(particle: Particle): Builder {
            super.withParticle(particle)
            return this
        }

        override fun <T> withParticle(particle: Particle, data: T): Builder {
            super.withParticle(particle, data)
            return this
        }

        override fun build(): PotionCloud {
            return PotionCloud(entity)
        }

        init {
            entity.duration = 10 * 20
        }
    }

    companion object {
        fun builder(targetEntity: Entity): Builder {
            val loc = targetEntity.location
            val entity = loc.world.spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD) as AreaEffectCloud
            targetEntity.addPassenger(entity)
            return Builder(entity)
        }

        fun builder(loc: Location): Builder {
            val entity = loc.world.spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD) as AreaEffectCloud
            return Builder(entity)
        }
    }
}