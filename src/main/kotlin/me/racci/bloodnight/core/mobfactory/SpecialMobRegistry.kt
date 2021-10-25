package me.racci.bloodnight.core.mobfactory

import me.racci.bloodnight.specialmobs.SpecialMob
import me.racci.bloodnight.specialmobs.mobs.creeper.*
import me.racci.bloodnight.specialmobs.mobs.enderman.FearfulEnderman
import me.racci.bloodnight.specialmobs.mobs.enderman.ToxicEnderman
import me.racci.bloodnight.specialmobs.mobs.events.*
import me.racci.bloodnight.specialmobs.mobs.phantom.FearfulPhantom
import me.racci.bloodnight.specialmobs.mobs.phantom.FirePhantom
import me.racci.bloodnight.specialmobs.mobs.phantom.PhantomSoul
import me.racci.bloodnight.specialmobs.mobs.skeleton.InvisibleSkeleton
import me.racci.bloodnight.specialmobs.mobs.skeleton.MagicSkeleton
import me.racci.bloodnight.specialmobs.mobs.slime.ToxicSlime
import me.racci.bloodnight.specialmobs.mobs.spider.BlazeRider
import me.racci.bloodnight.specialmobs.mobs.spider.SpeedSkeletonRider
import me.racci.bloodnight.specialmobs.mobs.spider.WitherSkeletonRider
import me.racci.bloodnight.specialmobs.mobs.witch.FireWizard
import me.racci.bloodnight.specialmobs.mobs.witch.ThunderWizard
import me.racci.bloodnight.specialmobs.mobs.witch.WitherWizard
import me.racci.bloodnight.specialmobs.mobs.zombie.ArmoredZombie
import me.racci.bloodnight.specialmobs.mobs.zombie.InvisibleZombie
import me.racci.bloodnight.specialmobs.mobs.zombie.SpeedZombie
import org.bukkit.entity.*
import org.bukkit.entity.EntityType.*
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Function

object SpecialMobRegistry {

    private val MOB_GROUPS      = HashMap<Class<out Entity>, MobGroup>()
    private val ENTITY_MAPPINGS = HashMap<Class<out Entity>, Class<out Entity>>()
    private val RAND            = ThreadLocalRandom.current()

    fun < T : SpecialMob<*> > registerMob(entityType: EntityType, clazz: Class<T>, factory: Function<LivingEntity, SpecialMob<*>>) {
        MOB_GROUPS.computeIfAbsent(entityType.entityClass!!) {MobGroup(entityType)}
            .registerFactory(clazz, factory)
    }

    fun getMobBaseClass(entity: Entity): Class<out Entity>? {
        if (entity.type == EntityType.UNKNOWN) return null
        return ENTITY_MAPPINGS.computeIfAbsent(entity.type.entityClass ?: return null) { c ->
            MOB_GROUPS.keys.firstOrNull{c.isAssignableFrom(it)}!!
        }

//        val mob = ENTITY_MAPPINGS.computeIfAbsent(entity.type.entityClass ?: return null) {
//            for (clazz in MOB_GROUPS.keys) {
//                if (clazz.isAssignableFrom(it)) {
//                    return@computeIfAbsent clazz
//                }
//            }
//            null
//        }
//        mob
    }

    fun getMobGroup(entity: Entity) =
        getMobBaseClass(entity).mapNotNull(MOB_GROUPS::get)

    fun getMobGroup(name: String) =
        MOB_GROUPS.entries.first{it.key.simpleName.equals(name, true)}

    val registeredMobs: Set<MobFactory>
        get() {
            val wrappers: MutableSet<MobFactory> = HashSet()
            for (value in MOB_GROUPS.values) {
                wrappers.addAll(value.factories)
            }
            return wrappers
        }

    /**
     * Get a read only map of registered mob groups
     *
     * @return registered mob groups
     */
    val mobGroups: Map<Class<out Entity>, MobGroup>
        get() = Collections.unmodifiableMap(MOB_GROUPS)

    fun getMobFactoryByName(arg: String) =
        registeredMobs.firstOrNull{it.mobName.equals(arg, true)}

    init {
        /*
        Initialize default mobs.
         */
        registerMob(
            CREEPER,
            EnderCreeper::class.java
        ) {EnderCreeper(it as Creeper)}
        registerMob(
            CREEPER,
            GhostCreeper::class.java
        ) {GhostCreeper(it as Creeper)}
        registerMob(
            CREEPER,
            NervousPoweredCreeper::class.java,
        ) {NervousPoweredCreeper(it as Creeper)}
        registerMob(
            CREEPER,
            SpeedCreeper::class.java,
        ) {SpeedCreeper(it as Creeper)}
        registerMob(
            CREEPER,
            ToxicCreeper::class.java,
        ) {ToxicCreeper(it as Creeper)}
        registerMob(
            CREEPER,
            UnstableCreeper::class.java,
        ) {UnstableCreeper(it as Creeper)}

        // Enderman
        registerMob(
            ENDERMAN,
            FearfulEnderman::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> FearfulEnderman(me.racci.bloodnight.core.mobfactory.e as Enderman?) })
        registerMob(
            ENDERMAN,
            ToxicEnderman::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> ToxicEnderman(me.racci.bloodnight.core.mobfactory.e as Enderman?) })

        // Phantom
        registerMob(
            PHANTOM,
            FearfulPhantom::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> FearfulPhantom(me.racci.bloodnight.core.mobfactory.e as Phantom?) })
        registerMob(
            PHANTOM,
            FirePhantom::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> FirePhantom(me.racci.bloodnight.core.mobfactory.e as Phantom?) })
        registerMob(
            PHANTOM,
            PhantomSoul::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> PhantomSoul(me.racci.bloodnight.core.mobfactory.e as Phantom?) })

        // Rider
        registerMob(
            SPIDER,
            BlazeRider::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> BlazeRider(me.racci.bloodnight.core.mobfactory.e as Spider?) })
        registerMob(
            SPIDER,
            SpeedSkeletonRider::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> SpeedSkeletonRider(me.racci.bloodnight.core.mobfactory.e as Spider?) })
        registerMob(
            SPIDER,
            WitherSkeletonRider::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> WitherSkeletonRider(me.racci.bloodnight.core.mobfactory.e as Spider?) })

        // Skeleton
        registerMob(
            SKELETON,
            InvisibleSkeleton::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> InvisibleSkeleton(me.racci.bloodnight.core.mobfactory.e as Skeleton?) })
        registerMob(
            SKELETON,
            MagicSkeleton::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> MagicSkeleton(me.racci.bloodnight.core.mobfactory.e as Skeleton?) })

        // Slime
        registerMob(
            SLIME,
            ToxicSlime::class.java,
        ) {ToxicSlime(it as Slime)}

        // Witch
        registerMob(
            WITCH,
            FireWizard::class.java,
        ) {FireWizard(it as Witch)}
        registerMob(
            WITCH,
            ThunderWizard::class.java,
        ) {ThunderWizard(it as Witch)}
        registerMob(
            WITCH,
            WitherWizard::class.java,
        ) {WitherWizard(it as Witch)}

        // Zombie
        registerMob(
            ZOMBIE,
            ArmoredZombie::class.java,
        ) {ArmoredZombie(it as Zombie)}
        registerMob(
            ZOMBIE,
            InvisibleZombie::class.java,
        ) {InvisibleZombie(it as Zombie)}
        registerMob(
            ZOMBIE,
            SpeedZombie::class.java,
        ) {SpeedZombie(it as Zombie)}

        /**
         * HollowsEve2021 Mobs
         */
        registerMob(
            ZOMBIE,
            HollowAdventurer::class.java
        ) {HollowAdventurer(it as Zombie)}
        registerMob(
            SKELETON,
            HollowArcher::class.java
        ) {HollowArcher(it as Skeleton)}
        registerMob(
            PHANTOM,
            HollowHaunter::class.java
        ) {HollowHaunter(it as Phantom)}
        registerMob(
            STRAY,
            HollowRider::class.java
        ) {HollowRider(it as Stray)}
        registerMob(
            HUSK,
            HollowGoliath::class.java
        ) {HollowGoliath(it as Husk)}
        registerMob(
            DROWNED,
            HollowNecromancer::class.java
        ) {HollowNecromancer(it as Drowned)}
        registerMob(
            SKELETON,
            HollowThrall::class.java
        ) {HollowThrall(it as Skeleton)}
        registerMob(
            WITHER_SKELETON,
            HollowHarbinger::class.java
        ) {HollowHarbinger(it as WitherSkeleton)}
    }
}