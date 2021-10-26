package me.racci.bloodnight.command.bloodnight.managedeathactions

import de.eldoria.eldoutilities.builder.ItemStackBuilder
import de.eldoria.eldoutilities.core.EldoUtilities
import de.eldoria.eldoutilities.inventory.ActionConsumer
import de.eldoria.eldoutilities.inventory.ActionItem
import de.eldoria.eldoutilities.inventory.InventoryActions
import de.eldoria.eldoutilities.localization.ILocalizer
import de.eldoria.eldoutilities.utils.DataContainerUtil
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.config.worldsettings.deathactions.PotionEffectSettings
import me.racci.bloodnight.config.worldsettings.deathactions.subsettings.LightningSettings
import me.racci.bloodnight.config.worldsettings.deathactions.subsettings.ShockwaveSettings
import me.racci.bloodnight.core.BloodNight
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffectType
import java.util.*

object DeathActionUtil {
    fun buildShockwaveUI(
        shockwave: ShockwaveSettings, player: Player, configuration: Configuration,
        localizer: ILocalizer, callback: Runnable
    ) {
        val inventory: Inventory = Bukkit.createInventory(
            player, 9,
            localizer.getMessage("manageDeathActions.inventory.shockwave.title")
        )
        val actions: InventoryActions = EldoUtilities.getInventoryActions()
            .wrap(player, inventory) {
                configuration.save()
                callback.run()
            }
        player.openInventory(inventory)

        // don't look at this O///O
        val valueKey = NamespacedKey(BloodNight.instance, "valueKey")
        val typeKey = NamespacedKey(BloodNight.instance, "typeKey")
        actions.addAction(
            ItemStackBuilder
                .of(Material.POTION)
                .withMetaValue(
                    PotionMeta::class.java
                ) { m: PotionMeta -> m.color = Color.RED }
                .withDisplayName(localizer.getMessage("manageDeathActions.inventory.shockwave.effects"))
                .build(),
            2,
            {
                player.closeInventory()
                EldoUtilities.getDelayedActions().schedule({
                    val effectInventory: Inventory = Bukkit.createInventory(
                        player, 54,
                        localizer.getMessage("manageDeathActions.inventory.shockwave.effects")
                    )
                    val effectActions: InventoryActions =
                        EldoUtilities.getInventoryActions().wrap(
                            player, effectInventory
                        ) {
                            configuration.save()
                            callback.run()
                        }
                    val respawnEffects: Map<PotionEffectType, PotionEffectSettings> = shockwave.shockwaveEffects
                    player.openInventory(effectInventory)

                    // this is always such a mess qwq
                    val values: Array<PotionEffectType> = PotionEffectType.values()
                    Arrays.sort(
                        values, Comparator.comparing { obj: PotionEffectType -> obj.name }
                    )
                    for ((pos, potionType) in values.withIndex()) {
                        val settings = respawnEffects[potionType]
                        effectActions.addAction(
                            ActionItem(
                                ItemStackBuilder
                                    .of(Material.POTION)
                                    .withDisplayName(potionType.name)
                                    .withMetaValue(
                                        PotionMeta::class.java
                                    ) { m: PotionMeta -> m.color = potionType.color }
                                    .withNBTData { c: PersistentDataContainer ->
                                        c.set(typeKey, PersistentDataType.STRING, potionType.name)
                                        c.set(
                                            valueKey,
                                            PersistentDataType.INTEGER,
                                            settings?.duration ?: 0
                                        )
                                    }
                                    .withLore(java.lang.String.valueOf(settings?.duration ?: 0))
                                    .build(),
                                pos,
                                ActionConsumer.getIntRange(valueKey, 0, 600)) label@
                            { it ->
                                if (it == null) return@label
                                val integer = DataContainerUtil.get(it, valueKey, PersistentDataType.INTEGER)
                                if (!integer.isPresent) return@label
                                val itemMeta: PotionMeta = it.itemMeta as PotionMeta
                                val optionalType =
                                    DataContainerUtil.get(it, typeKey, PersistentDataType.STRING)
                                optionalType.ifPresent {
                                    val type = PotionEffectType.getByName(it) ?: return@ifPresent
                                    if (integer.get() == 0) {
                                        shockwave.shockwaveEffects.remove(type)
                                        return@ifPresent
                                    }
                                    shockwave.shockwaveEffects
                                        .compute(type) { _, _ -> PotionEffectSettings(type, integer.get()) }
                                }
                            }
                        )
                    }
                }, 2)
            },
            {}
        )
        actions.addAction(
            ItemStackBuilder
                .of(Material.CLOCK)
                .withDisplayName(
                    localizer.getMessage("manageDeathActions.inventory.shockwave.minEffectDuration")
                )
                .withLore((shockwave.minDuration.toInt() * 100).toString())
                .withNBTData { c: PersistentDataContainer ->
                    c.set(
                        valueKey,
                        PersistentDataType.INTEGER,
                        shockwave.minDuration.toInt() * 100
                    )
                }
                .build(),
            3,
            ActionConsumer.getIntRange(valueKey, 0, 100)
        ) {
            val integer = it!!.itemMeta.persistentDataContainer.get(
                valueKey,
                PersistentDataType.INTEGER
            )
            if (integer != null) {
                shockwave.minDuration = integer / 100.0
            }
        }
        actions.addAction(
            ItemStackBuilder
                .of(Material.BOW)
                .withDisplayName(localizer.getMessage("field.range"))
                .withLore(java.lang.String.valueOf(shockwave.shockwaveRange))
                .withNBTData { c: PersistentDataContainer ->
                    c.set(
                        valueKey,
                        PersistentDataType.INTEGER,
                        shockwave.shockwaveRange
                    )
                }
                .build(),
            4,
            ActionConsumer.getIntRange(valueKey, 0, 60)
        ) {
            shockwave.shockwaveRange = (
                    DataContainerUtil.getOrDefault(
                        it,
                        valueKey,
                        PersistentDataType.INTEGER,
                        0
                    )
                    )
        }
        actions.addAction(
            ItemStackBuilder
                .of(Material.BLAZE_POWDER)
                .withDisplayName(localizer.getMessage("field.power"))
                .withLore(java.lang.String.valueOf(shockwave.shockwavePower))
                .withNBTData { c: PersistentDataContainer ->
                    c.set(
                        valueKey,
                        PersistentDataType.INTEGER,
                        shockwave.shockwavePower
                    )
                }
                .build(),
            5,
            ActionConsumer.getIntRange(valueKey, 0, 60)
        ) {
            shockwave.shockwavePower = (
                    DataContainerUtil.getOrDefault(
                        it,
                        valueKey,
                        PersistentDataType.INTEGER,
                        0
                    )
                    )
        }
        actions.addAction(
            ItemStackBuilder
                .of(Material.LEVER)
                .withDisplayName(localizer.getMessage("field.probability"))
                .withLore(java.lang.String.valueOf(shockwave.shockwaveProbability))
                .withNBTData { c: PersistentDataContainer ->
                    c.set(
                        valueKey,
                        PersistentDataType.INTEGER,
                        shockwave.shockwaveProbability
                    )
                }
                .build(),
            6,
            ActionConsumer.getIntRange(valueKey, 0, 100)
        ) {
            shockwave.shockwaveProbability = (
                    DataContainerUtil.getOrDefault(
                        it,
                        valueKey,
                        PersistentDataType.INTEGER,
                        0
                    )
                    )
        }
    }

    fun buildLightningUI(
        lightningSettings: LightningSettings, player: Player, configuration: Configuration,
        localizer: ILocalizer, callback: Runnable
    ) {
        val inventory: Inventory = Bukkit.createInventory(
            player, 18,
            localizer.getMessage("manageDeathActions.inventory.lightning.title")
        )
        val actions: InventoryActions = EldoUtilities.getInventoryActions()
            .wrap(player, inventory) {
                configuration.save()
                callback.run()
            }
        val valueKey = NamespacedKey(BloodNight.instance, "valueKey")
        actions.addAction(
            ActionItem(
                ItemStackBuilder
                    .of(Material.LEVER)
                    .withDisplayName(
                        localizer.getMessage("manageDeathActions.inventory.lightning.lightningActive")
                    )
                    .withLore(java.lang.String.valueOf(lightningSettings.doLightning))
                    .withNBTData { c: PersistentDataContainer ->
                        c.set(
                            valueKey,
                            PersistentDataType.BYTE,
                            DataContainerUtil.booleanToByte(lightningSettings.doLightning)
                        )
                    }
                    .build(),
                3,
                ActionConsumer.booleanToggle(valueKey)
            ) {
                val fieldValue: Boolean = DataContainerUtil.byteToBoolean(
                    DataContainerUtil.compute(
                        it,
                        valueKey,
                        PersistentDataType.BYTE
                    ) { s: Byte? -> s }
                )
                lightningSettings.doLightning = fieldValue
            }
        )
        actions.addAction(
            ActionItem(
                ItemStackBuilder
                    .of(Material.LEVER)
                    .withDisplayName(
                        localizer.getMessage("manageDeathActions.inventory.lightning.thunderActive")
                    )
                    .withLore(java.lang.String.valueOf(lightningSettings.doThunder))
                    .withNBTData { c: PersistentDataContainer ->
                        c.set(
                            valueKey,
                            PersistentDataType.BYTE,
                            DataContainerUtil.booleanToByte(lightningSettings.doThunder)
                        )
                    }
                    .build(),
                5,
                ActionConsumer.booleanToggle(valueKey)
            ) {
                val fieldValue: Boolean = DataContainerUtil.byteToBoolean(
                    DataContainerUtil.compute(
                        it,
                        valueKey,
                        PersistentDataType.BYTE
                    ) { s: Byte? -> s }
                )
                lightningSettings.doThunder = fieldValue
            }
        )
        actions.addAction(
            ActionItem(
                ItemStackBuilder
                    .of(Material.BLAZE_ROD)
                    .withDisplayName(
                        localizer.getMessage("manageDeathActions.inventory.lightning.lightningProb")
                    )
                    .withLore(java.lang.String.valueOf(lightningSettings.lightning))
                    .withNBTData { c: PersistentDataContainer ->
                        c.set(
                            valueKey,
                            PersistentDataType.INTEGER,
                            lightningSettings.lightning
                        )
                    }
                    .build(),
                12,
                ActionConsumer.getIntRange(valueKey, 0, 100)
            ) {
                val fieldValue = DataContainerUtil.compute(
                    it,
                    valueKey,
                    PersistentDataType.INTEGER
                ) { s: Int -> s }
                lightningSettings.lightning = fieldValue ?: 0
            }
        )
        actions.addAction(
            ActionItem(
                ItemStackBuilder
                    .of(Material.BLAZE_POWDER)
                    .withDisplayName(
                        localizer.getMessage("manageDeathActions.inventory.lightning.thunderProb")
                    )
                    .withLore(java.lang.String.valueOf(lightningSettings.thunder))
                    .withNBTData { c: PersistentDataContainer ->
                        c.set(
                            valueKey,
                            PersistentDataType.INTEGER,
                            lightningSettings.thunder
                        )
                    }
                    .build(),
                14,
                ActionConsumer.getIntRange(valueKey, 0, 100)
            ) {
                val fieldValue = DataContainerUtil.compute(
                    it,
                    valueKey,
                    PersistentDataType.INTEGER
                ) { s: Int? -> s }
                lightningSettings.thunder = fieldValue ?: 0
            }
        )
        player.openInventory(inventory)
    }
}