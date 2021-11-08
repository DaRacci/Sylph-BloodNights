package me.racci.bloodnight.config.worldsettings.mobsettings

import de.eldoria.eldoutilities.serialization.SerializationUtil
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.core.mobfactory.MobFactory
import me.racci.bloodnight.core.mobfactory.SpecialMobRegistry
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ThreadLocalRandom

@SerializableAs("bloodNightMobSettings")
class MobSettings : ConfigurationSerializable {
    var vanillaMobSettings = VanillaMobSettings(); private set

    /**
     * Enabled or disables mob names for special mobs.
     */
    var displayMobNames = true

    /**
     * The modifier which will be multiplied with monster damage when dealing damage to players.
     */
    var damageMultiplier = 2.0

    /**
     * The modifier which will be applied to special Mobs health on spawn.
     */
    var healthModifier = 2.0

    /**
     * The modifier which will be multiplied with the dropped exp of a monster.
     */
    var experienceMultiplier = 4.0

    /**
     * Sleep time will be set for every player when the nights start and will be reset to earlier value when the night
     * ends
     */
    var forcePhantoms = true

    /**
     * The conversion rate of mobs. Higher number -> more special mobs.
     */
    var spawnPercentage = 50

    /**
     * The general drops during blood night.
     */
    var defaultDrops: List<Drop> = ArrayList()

    /**
     * If true drops will be added to vanilla drops. If false vanilla drops will be removed.
     */
    var naturalDrops = true

    /**
     * Max Amount of custom drops which can be dropped on death.
     */
    var dropAmount = 3

    /**
     * List of mob type settings.
     */
    var mobTypes = MobTypes()

    constructor(objectMap: Map<String, Any>) {
        val map = SerializationUtil.mapOf(objectMap)
        vanillaMobSettings = map.getValueOrDefault("vanillaMobSettings", vanillaMobSettings)
        displayMobNames = map.getValueOrDefault("displayMobNames", displayMobNames)
        damageMultiplier = map.getValueOrDefault("damageMultiplier", damageMultiplier)
        healthModifier = map.getValueOrDefault("healthMultiplier", healthModifier)
        experienceMultiplier = map.getValueOrDefault("experienceMultiplier", experienceMultiplier)
        forcePhantoms = map.getValueOrDefault("forcePhantoms", forcePhantoms)
        spawnPercentage = map.getValueOrDefault("spawnPercentage", spawnPercentage)
        defaultDrops = map.getValueOrDefault("drops", defaultDrops)
        naturalDrops = map.getValueOrDefault("naturalDrops", naturalDrops)
        dropAmount = map.getValueOrDefault("dropAmount", dropAmount)
        mobTypes = map.getValueOrDefault("mobTypes", mobTypes)
    }

    constructor()

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

    fun isActive(mobName: String) =
        getMobByName(mobName)?.active ?: false

    fun getDrops(mobSetting: MobSetting): List<ItemStack> {
        val totalDrops = ArrayList(mobSetting.drops)
        if (!mobSetting.overrideDefaultDrops) {
            totalDrops.addAll(defaultDrops)
        }
        return getDrops(totalDrops, 1, mobSetting.getOverriddenDropAmount(dropAmount))
    }

    /**
     * Get the amount of drops from the default drops
     *
     * @param dropAmount max amount of drops
     * @return list of length between 0 and drop amount
     */
    fun getDrops(dropAmount: Int) =
        getDrops(defaultDrops, 0, dropAmount)

    /**
     * Get the amount of drops from a list of weighted drops
     *
     * @param totalDrops list of drops
     * @param dropAmount max amount of drops
     * @param minDrops   the min amount of drops
     * @return item stack list of length between 1 and drop amount
     */
    private fun getDrops(totalDrops: List<Drop>, minDrops: Int, dropAmount: Int): List<ItemStack> {
        if (dropAmount == 0) return ArrayList()
        val totalWeight = totalDrops.stream().mapToInt(Drop::weight).sum()
        val current: ThreadLocalRandom = ThreadLocalRandom.current()
        val nextInt: Int = current.nextInt(minDrops, dropAmount + 1)
        val result: MutableList<ItemStack> = ArrayList()
        var currentWeight = 0
        for (i in 0 until nextInt) {
            val goal: Int = current.nextInt(totalWeight + 1)
            for (drop in totalDrops) {
                currentWeight += drop.weight
                if (currentWeight < goal) continue
                result.add(ItemStack(drop.item.clone()))
                break
            }
        }
        return result
    }

    /*private val hollowsEveDrops : ArrayList<WeightedDrops> = object : ArrayList<WeightedDrops>() {
        init {
            addAll(listOf(
                WeightedDrops(ItemFactory[me.racci.hollowseve.enums.HollowsEve2021.CANDY_CORN].asQuantity(4), 10),
                WeightedDrops(ItemStack(Material.IRON_INGOT).asQuantity(2), 10),
                WeightedDrops(ItemStack(Material.BONE).asQuantity(3), 30),
                WeightedDrops(HollowsEve2021.hollowsKey, 5),
                WeightedDrops(ItemStack(Material.NETHERITE_SCRAP), 3),
            ))
        }
    }

    data class WeightedDrops(val item: ItemStack, val weight: Int)

    private fun getDrops() : List<ItemStack> {
        val current: ThreadLocalRandom = ThreadLocalRandom.current()
        val nextInt: Int = current.nextInt(1, 3)
        val result: ArrayList<ItemStack> = ArrayList()
        var currentWeight = 0
        for (i in 0 until nextInt) {
            val goal: Int = current.nextInt(101)
            for (drop in hollowsEveDrops) {
                currentWeight += drop.weight
                if (currentWeight < goal) continue
                result.add(ItemStack(drop.item.clone()))
                break
            }
        }
        return result
    }*/

    fun getMobByName(string: String): MobSetting? {
        val mobFactoryByName = SpecialMobRegistry.getMobFactoryByName(string) ?: return null
        val name = mobFactoryByName.entityType.entityClass?.simpleName
        for (entry in mobTypes.mobSettings.getOrDefault(name, emptySet())) {
            if (string.equals(entry.mobName, ignoreCase = true)) {
                return entry
            }
        }
        return null
    }

    @SerializableAs("bloodNightMobTypes")
    class MobTypes : ConfigurationSerializable {
        /**
         * List of mob type settings.
         */
        var mobSettings: HashMap<String, HashSet<MobSetting>> = HashMap()

        constructor() {
            mobSettings = SpecialMobRegistry.mobGroups.entries.associate { map ->
                map.key.simpleName to object : HashSet<MobSetting>() {init {
                    map.value.factories.map(MobFactory::mobName).forEach { add(MobSetting(it)) }
                }
                }
            } as HashMap<String, HashSet<MobSetting>>
            // Old Method
//            mobSettings = SpecialMobRegistry.mobGroups.entries.stream()
//                .map { m ->
//                    val pair: Pair<String, MutableSet<MobSetting>> = Pair(m.getKey().getSimpleName(), HashSet<Any>())
//                    m.getValue().getFactories().forEach { v -> pair.second.add(MobSetting(v.getMobName())) }
//                    pair
//                }
//                .collect(Collectors.toMap(Function<T, K> { p: T -> p.first }, Function<T, U> { p: T -> p.second }))
        }

        constructor(objectMap: Map<String, Any>) {
            val map = SerializationUtil.mapOf(objectMap)
            SpecialMobRegistry.mobGroups.entries.forEach {
                val ms = mobSettings.computeIfAbsent(it.key.simpleName) { HashSet() }
                val value = map.getValueOrDefault(it.key.simpleName, ArrayList<MobSetting>())
                it.value.factories.forEach { f ->
                    var found = false
                    value.first { msv ->
                        if (msv.mobName.equals(f.mobName, true)) {
                            ms.add(msv); found = true; true
                        } else false
                    }
                    if (!found) {
                        ms.add(MobSetting(f.mobName))
                        BloodNight.logger().info("No settings for ${f.mobName} found. Creating default settings.")
                    }
                }
            }
        }

        override fun serialize(): MutableMap<String, Any> =
            SerializationUtil.newBuilder().apply {
                mobSettings.forEach { add(it.key, ArrayList(it.value)) }
            }.build()

        /**
         * Returns an optional of a mob group.
         *
         * @param groupName name of group
         * @return optional result set. Key represents the mob group and value a set of mob settings.
         */
        fun getGroup(groupName: String) =
            mobSettings.entries.firstOrNull { it.key.equals(groupName, true) }

        val settings: Set<MobSetting>
            get() = object : HashSet<MobSetting>() {init {
                mobSettings.values.forEach(::addAll)
            }
            }
    }
}