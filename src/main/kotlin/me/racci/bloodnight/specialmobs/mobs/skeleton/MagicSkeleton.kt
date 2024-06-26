package me.racci.bloodnight.specialmobs.mobs.skeleton

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractSkeleton
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Skeleton
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.concurrent.ThreadLocalRandom

class MagicSkeleton(skeleton: Skeleton) : AbstractSkeleton(skeleton) {

    private val rand: ThreadLocalRandom = ThreadLocalRandom.current()

    override fun tick() {
        SpecialMobUtil.addPotionEffect(baseEntity, PotionEffectType.NIGHT_VISION, 1, true)
    }

    override fun onProjectileHit(event: ProjectileHitEvent) {
        val hitEntity: Entity = event.hitEntity ?: return
        if (hitEntity is LivingEntity) {
            hitEntity.addPotionEffect(randomEffect)
        }
    }

    private val randomEffect: PotionEffect
        get() = EFFECTS[rand.nextInt(EFFECTS.size)]

    companion object {
        private val EFFECTS: Array<PotionEffect> = arrayOf(
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