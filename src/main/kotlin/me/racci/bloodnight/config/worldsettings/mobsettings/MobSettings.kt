package me.racci.bloodnight.config.worldsettings.mobsettings

import de.eldoria.bloodnight.core.BloodNight
import de.eldoria.eldoutilities.container.Pair
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import kotlin.collections.HashMap
import kotlin.collections.HashSet

@Getter
@Setter
@SerializableAs("bloodNightMobSettings")
class MobSettings : ConfigurationSerializable {
    private var vanillaMobSettings = VanillaMobSettings()

    /**
     * Enabled or disables mob names for special mobs.
     */
    private var displayMobNames = true

    /**
     * The modifier which will be multiplied with monster damage when dealing damage to players.
     */
    private var damageMultiplier = 2.0

    /**
     * The modifier which will be applied to special Mobs health on spawn.
     */
    private var healthModifier = 2.0

    /**
     * The modifier which will be muliplied with the dropped exp of a monster.
     */
    private var experienceMultiplier = 4.0

    /**
     * Sleep time will be set for every player when the nights starts and will be reset to earlier value when the night
     * ends
     */
    private var forcePhantoms = true

    /**
     * The conversion rate of mobs. Higher numer -> more special mobs.
     */
    private var spawnPercentage = 50

    /**
     * The general drops during blood night.
     */
    private var defaultDrops: List<Drop> = ArrayList()

    /**
     * If true drops will be added to vanilla drops. If false vanilla drops will be removed.
     */
    private var naturalDrops = true

    /**
     * Max Amount of custom drops which can be dropped on death.
     */
    private var dropAmount = 3

    /**
     * List of mob type settings.
     */
    private var mobTypes = MobTypes()

    constructor(objectMap: Map<String?, Any?>?) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        vanillaMobSettings = map.getValueOrDefault<VanillaMobSettings>("vanillaMobSettings", vanillaMobSettings)
        displayMobNames = map.getValueOrDefault<Boolean>("displayMobNames", displayMobNames)
        damageMultiplier = map.getValueOrDefault<Double>("damageMultiplier", damageMultiplier)
        healthModifier = map.getValueOrDefault<Double>("healthMultiplier", healthModifier)
        experienceMultiplier = map.getValueOrDefault<Double>("experienceMultiplier", experienceMultiplier)
        forcePhantoms = map.getValueOrDefault<Boolean>("forcePhantoms", forcePhantoms)
        spawnPercentage = map.getValueOrDefault<Int>("spawnPercentage", spawnPercentage)
        defaultDrops = map.getValueOrDefault<List<Drop>>("drops", defaultDrops)
        naturalDrops = map.getValueOrDefault<Boolean>("naturalDrops", naturalDrops)
        dropAmount = map.getValueOrDefault<Int>("dropAmount", dropAmount)
        mobTypes = map.getValueOrDefault<MobTypes>("mobTypes", mobTypes)
    }

    constructor() {}

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("vanillaMobSettings", vanillaMobSettings)
            .add("displayMobNames", displayMobNames)
            .add("damageMultiplier", damageMultiplier)
            .add("healthMultiplier", healthModifier)
            .add("experienceMultiplier", experienceMultiplier)
            .add("forcePhantoms", forcePhantoms)
            .add("spawnPercentage", spawnPercentage)
            .add("drops", defaultDrops)
            .add("naturalDrops", naturalDrops)
            .add("dropAmount", dropAmount)
            .add("mobTypes", mobTypes)
            .build()
    }

    fun isActive(mobName: String): Boolean {
        return getMobByName(mobName).map<Any>(MobSetting::isActive).orElse(false)
    }

    fun getDrops(mobSetting: MobSetting): List<ItemStack> {
        val totalDrops: MutableList<Drop> = ArrayList<Any?>(mobSetting.getDrops())
        if (!mobSetting.isOverrideDefaultDrops()) {
            totalDrops.addAll(defaultDrops)
        }
        return getDrops(totalDrops, 1, mobSetting.getOverridenDropAmount(dropAmount))
    }

    /**
     * Get the amount of drops from the default drops
     *
     * @param dropAmount max amount of drops
     * @return list of length between 0 and drop amount
     */
    fun getDrops(dropAmount: Int): List<ItemStack> {
        return getDrops(defaultDrops, 0, dropAmount)
    }

    /**
     * Get the amount of drops from a list of weighted drops
     *
     * @param totalDrops list of drops
     * @param dropAmount max amount of drops
     * @param minDrops   the min amount of drops
     * @return item stack list of length between 1 and drop amount
     */
    fun getDrops(totalDrops: List<Drop>, minDrops: Int, dropAmount: Int): List<ItemStack> {
        if (dropAmount == 0) return ArrayList<ItemStack>()
        val totalWeight = totalDrops.stream().mapToInt(Drop::getWeight).sum()
        val current: ThreadLocalRandom = ThreadLocalRandom.current()
        val nextInt: Int = current.nextInt(minDrops, dropAmount + 1)
        val result: MutableList<ItemStack> = ArrayList<ItemStack>()
        var currentWeight = 0
        for (i in 0 until nextInt) {
            val goal: Int = current.nextInt(totalWeight + 1)
            for (drop in totalDrops) {
                currentWeight += drop.getWeight()
                if (currentWeight < goal) continue
                result.add(ItemStack(drop.item.clone()))
                break
            }
        }
        return result
    }

    fun getMobByName(string: String): Optional<MobSetting> {
        val mobFactoryByName: Optional<MobFactory> = SpecialMobRegistry.getMobFactoryByName(string)
        if (!mobFactoryByName.isPresent()) return Optional.empty()
        val name: String = mobFactoryByName.get().getEntityType().getEntityClass().getSimpleName()
        for (entry in mobTypes.mobSettings.getOrDefault(name, emptySet())) {
            if (string.equals(entry.getMobName(), ignoreCase = true)) {
                return Optional.of(entry)
            }
        }
        return Optional.empty()
    }

    @SerializableAs("bloodNightMobTypes")
    class MobTypes : ConfigurationSerializable {
        /**
         * List of mob type settings.
         */
        var mobSettings: MutableMap<String, MutableSet<MobSetting>> = HashMap()

        constructor() {
            mobSettings = SpecialMobRegistry.getMobGroups().entrySet().stream()
                .map { m ->
                    val pair: Pair<String, MutableSet<MobSetting>> = Pair(m.getKey().getSimpleName(), HashSet<Any>())
                    m.getValue().getFactories().forEach { v -> pair.second.add(MobSetting(v.getMobName())) }
                    pair
                }
                .collect(Collectors.toMap(Function<T, K> { p: T -> p.first }, Function<T, U> { p: T -> p.second }))
        }

        constructor(objectMap: Map<String?, Any?>?) {
            val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
            for ((key, value) in SpecialMobRegistry.getMobGroups().entrySet()) {
                val mobSettings = mobSettings.computeIfAbsent(key.simpleName) { k: String? -> HashSet() }
                val valueOrDefault: List<MobSetting> = map.getValueOrDefault<ArrayList<MobSetting>>(
                    key.simpleName, ArrayList<MobSetting>()
                )

                // only load settings for valid mobs
                for (factory in value.getFactories()) {
                    var found = false
                    for (mobSetting in valueOrDefault) {
                        // check if a setting is already registered
                        if (mobSetting.getMobName().equalsIgnoreCase(factory.getMobName())) {
                            mobSettings.add(mobSetting)
                            found = true
                            break
                        }
                    }
                    if (!found) {
                        // create default settings
                        mobSettings.add(MobSetting(factory.getMobName()))
                        BloodNight.logger().info(
                            java.lang.String.format(
                                "No settings for %s found. Creating default settings.",
                                factory.getMobName()
                            )
                        )
                    }
                }
            }
        }

        override fun serialize(): Map<String, Any> {
            val builder: SerializationUtil.Builder = SerializationUtil.newBuilder()
            for ((key, value) in mobSettings) {
                builder.add(key, ArrayList(value))
            }
            return builder.build()
        }

        /**
         * Returns a optional of a mob group.
         *
         * @param groupName name of group
         * @return optional result set. Key represents the mob group and value a set of mob settings.
         */
        fun getGroup(groupName: String?): Optional<Map.Entry<String, Set<MobSetting>>> {
            return mobSettings.entries.stream()
                .filter { (key): Map.Entry<String, Set<MobSetting>> -> key.equals(groupName, ignoreCase = true) }
                .findFirst()
        }

        val settings: Set<MobSetting>
            get() {
                val settings: MutableSet<MobSetting> = HashSet()
                mobSettings.values.forEach(Consumer<Set<MobSetting>> { c: Set<MobSetting>? ->
                    settings.addAll(
                        c!!
                    )
                })
                return settings
            }
    }
}