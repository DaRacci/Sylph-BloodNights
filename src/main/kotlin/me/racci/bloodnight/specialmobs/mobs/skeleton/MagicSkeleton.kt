package me.racci.bloodnight.specialmobs.mobs.skeleton

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractSkeleton
import org.bukkit.entity.Entity
import org.bukkit.entity.Skeleton

class MagicSkeleton(skeleton: Skeleton?) : AbstractSkeleton(skeleton) {
    private val rand: ThreadLocalRandom = ThreadLocalRandom.current()
    fun tick() {
        SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.NIGHT_VISION, 1, true)
    }

    fun onProjectileHit(event: ProjectileHitEvent) {
        val hitEntity: Entity = event.getHitEntity()
        if (hitEntity is LivingEntity) {
            (hitEntity as LivingEntity).addPotionEffect(randomEffect)
        }
    }

    private val randomEffect: PotionEffect
        private get() = EFFECTS[rand.nextInt(EFFECTS.size)]

    companion object {
        private val EFFECTS: Array<PotionEffect> = arrayOf<PotionEffect>(
            PotionEffect(PotionEffectType.SLOW, 8 * 20, 1, true, true),
            PotionEffect(PotionEffectType.SLOW, 6 * 20, 2, true, true),
            PotionEffect(PotionEffectType.HARM, 2 * 20, 1, true, true),
            PotionEffect(PotionEffectType.CONFUSION, 5 * 20, 1, true, true),
            PotionEffect(PotionEffectType.LEVITATION, 5 * 20, 1, true, true),
            PotionEffect(PotionEffectType.WEAKNESS, 5 * 20, 1, true, true),
            PotionEffect(PotionEffectType.POISON, 6 * 20, 1, true, true),
            PotionEffect(PotionEffectType.POISON, 4 * 20, 2, true, true)
        )
    }
}