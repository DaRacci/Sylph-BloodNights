package me.racci.bloodnight.config.worldsettings

import de.eldoria.eldoutilities.container.Pair
import de.eldoria.eldoutilities.serialization.SerializationUtil
import de.eldoria.eldoutilities.serialization.TypeResolvingMap
import de.eldoria.eldoutilities.utils.EMath
import de.eldoria.eldoutilities.utils.EnumUtil
import me.racci.bloodnight.config.worldsettings.NightSelection.NightSelectionType.*
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.core.manager.nightmanager.util.NightUtil.getMoonPhase
import me.racci.bloodnight.util.MoonPhase
import org.bukkit.World
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.time.Instant
import java.util.*
import java.util.logging.Level
import java.util.stream.Collectors

@SerializableAs("bloodNightNightSelection")
class NightSelection : ConfigurationSerializable {
    var nightSelectionType = RANDOM

    /**
     * Probability that a night becomes a blood night. In percent 0-100.
     */
    var probability = 60
    var moonPhase: MutableMap<Int, Int> = object : HashMap<Int, Int>() {
        init {
            put(0, 0)
            put(1, 10)
            put(2, 20)
            put(3, 40)
            put(4, 100)
            put(5, 40)
            put(6, 20)
            put(7, 10)
        }
    }
    var phaseCustom: MutableMap<Int, Int> = object : HashMap<Int, Int>() {
        init {
            put(0, 50)
            put(1, 50)
            put(2, 50)
        }
    }
    private var currPhase = 0

    /**
     * Length of a period.
     */
    var period = 10
        set(period) {
            field = period
            currCurvePos = 0
        }

    /**
     * Current value of the curve.
     */
    private var currCurvePos = 0

    /**
     * Min curve value.
     */
    var minCurveVal = 20

    /**
     * Max curve value.
     */
    var maxCurveVal = 80

    /**
     * Interval of days.
     */
    var interval = 5

    /**
     * Probability on interval day.
     */
    var intervalProbability = 100

    /**
     * Current interval
     */
    private var curInterval = 0

    constructor(objectMap: Map<String, Any>) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        nightSelectionType = map.getValueOrDefault("nightSelectionType", nightSelectionType) {
            EnumUtil.parse(
                it,
                NightSelectionType::class.java
            )
        }
        // probability
        probability = map.getValueOrDefault("probability", probability)
        // phases
        moonPhase = parsePhase(map.getValueOrDefault("phases", moonPhase.entries.map { "${it.key}:${it.value}" }))
        phaseCustom =
            parsePhase(map.getValueOrDefault("phasesCustom", phaseCustom.entries.map { "${it.key}:${it.value}" }))
        verifyPhases()
        currPhase = map.getValueOrDefault("currPhase", currPhase)
        period = map.getValueOrDefault("period", period)
        // curve
        currCurvePos = map.getValueOrDefault("currCurvePos", currCurvePos)
        minCurveVal = map.getValueOrDefault("minCurveVal", minCurveVal)
        maxCurveVal = map.getValueOrDefault("maxCurveVal", maxCurveVal)
        // interval
        interval = map.getValueOrDefault("interval", interval)
        intervalProbability = map.getValueOrDefault("intervalProbability", intervalProbability)
        curInterval = map.getValueOrDefault("curInterval", curInterval)
    }

    constructor()

    private fun upcountInterval() {
        curInterval++
        curInterval %= interval
    }

    private fun upcountPhase() {
        currPhase++
        currPhase %= phaseCustom.size
    }

    private fun upcountCurve() {
        currCurvePos++
        currCurvePos %= period
    }

    private fun getPhaseProbability(phase: Int): Int {
        return moonPhase.getOrDefault(phase, -1)
    }

    override fun serialize(): Map<String, Any> {
        val phases = moonPhase.entries.map { "${it.key}:${it.value}" }
        val phasesCustom = phaseCustom.entries.stream().map { "${it.key}:${it.value}" }
        return SerializationUtil.newBuilder()
            .add("probability", probability)
            .add("nightSelectionType", nightSelectionType.name)
            .add("phases", phases)
            .add("phasesCustom", phasesCustom)
            .add("currPhase", currPhase)
            .add("period", period)
            .add("currCurvePos", currCurvePos)
            .add("minCurveVal", minCurveVal)
            .add("maxCurveVal", maxCurveVal)
            .add("interval", interval)
            .add("intervalProbability", intervalProbability)
            .add("curInterval", curInterval)
            .build()
    }

    fun setPhaseCustom(phase: Int, probability: Int) {
        phaseCustom[phase] = probability
        currPhase = phaseCustom.size.coerceAtMost(currPhase)
    }

    fun setMoonPhase(phase: Int, probability: Int) {
        moonPhase[phase] = probability
    }

    fun setPhaseCount(phaseCount: Int) {
        val newPhases: MutableMap<Int, Int> = HashMap()
        for (i in 0 until phaseCount) {
            newPhases[i] = phaseCustom.getOrDefault(i, 50)
        }
        phaseCustom = newPhases
        currPhase = phaseCustom.size.coerceAtMost(currPhase)
    }

    private fun verifyPhases() {
        val newPhases: MutableMap<Int, Int> = HashMap()
        for (i in 0 until phaseCustom.size.coerceAtMost(54)) {
            newPhases[i] = phaseCustom.getOrDefault(i, 50)
        }
        phaseCustom = newPhases
        currPhase = phaseCustom.size.coerceAtMost(currPhase)
    }

    private fun parsePhase(list: List<String>): MutableMap<Int, Int> {
        val map: MutableMap<Int, Int> = HashMap()
        for (s in list) {
            val split = s.split(":").toTypedArray()
            try {
                map[split[0].toInt()] = when (split.size) {
                    1 -> 100
                    2 -> split[1].toInt()
                    else -> {
                        BloodNight.logger().log(Level.WARNING, "Could not parse $s to phase."); continue
                    }
                }
            } catch (e: NumberFormatException) {
                BloodNight.logger().log(Level.WARNING, "Could not parse $s to phase.")
            }
        }
        return map
    }

    fun getCurrentProbability(world: World): Int {
        return getNextProbability(world, 0)
    }

    fun getNextProbability(world: World, nightOffset: Int) =
        when (nightSelectionType) {
            RANDOM -> probability
            MOON_PHASE -> {
                val phase = getMoonPhase(world)
                if (moonPhase.containsKey(phase)) 0 else getPhaseProbability((phase + nightOffset) % 8)
            }
            REAL_MOON_PHASE -> {
                val cal = Calendar.getInstance()
                cal.time = Date.from(Instant.now())
                val phase = (MoonPhase.computePhaseIndex(cal) + 4) % 8
                if (moonPhase.containsKey(phase)) 0 else getPhaseProbability(phase)
            }
            INTERVAL -> if ((curInterval + nightOffset) % interval != 0) 0 else intervalProbability
            PHASE -> phaseCustom[(currPhase + nightOffset) % phaseCustom.size]!!
            CURVE -> {
                val curveProb: Double
                val pos = (currCurvePos + nightOffset) % period
                curveProb = if (pos <= period / 2) {
                    EMath.smoothCurveValue(
                        Pair.of(0.0, minCurveVal.toDouble()),
                        Pair.of(period.toDouble() / 2, maxCurveVal.toDouble()), pos.toDouble()
                    )
                } else {
                    EMath.smoothCurveValue(
                        Pair.of(period.toDouble() / 2, maxCurveVal.toDouble()),
                        Pair.of(period.toDouble(), minCurveVal.toDouble()), pos.toDouble()
                    )
                }
                curveProb.toInt()
            }
        }


    fun upcount() {
        when (nightSelectionType) {
            RANDOM -> {}
            MOON_PHASE -> {}
            REAL_MOON_PHASE -> {}
            INTERVAL -> upcountInterval()
            PHASE -> upcountPhase()
            CURVE -> upcountCurve()
        }
    }

    enum class NightSelectionType {
        /**
         * Determine bloodnight based on a random value.
         */
        RANDOM,

        /**
         * Determine bloodnight based on a random value attached to the in-game moon phase.
         */
        MOON_PHASE,

        /**
         * Determine bloodnight based on the real moon phase and a random value attached to the phase.
         */
        REAL_MOON_PHASE,

        /**
         * Determine bloodnight based on an interval with a random value.
         */
        INTERVAL,

        /**
         * Determine bloodnight based on a random value attached to a phase.
         */
        PHASE,

        /**
         * Determine bloodnight based on a smooth curve with a fixed length and a max and min probability.
         */
        CURVE
    }

    override fun toString() =
        when (nightSelectionType) {
            RANDOM -> "Mode: $nightSelectionType | Prob: $probability"
            MOON_PHASE, REAL_MOON_PHASE ->
                "Mode: $nightSelectionType | Phases: ${
                    moonPhase.entries
                        .stream()
                        .map { "${it.key}:${it.value}" }
                        .collect(Collectors.joining(" | "))
                }"
            INTERVAL -> "Mode: $nightSelectionType | Interval $curInterval of ${interval - 1} with Prob $intervalProbability"
            PHASE ->
                "Mode: $nightSelectionType | Phases: ${
                    phaseCustom.entries
                        .stream()
                        .map { "${it.key}:${it.value}" }
                        .collect(Collectors.joining(" | "))
                }"
            CURVE -> "Mode: $nightSelectionType | Pos $currCurvePos on curve between $minCurveVal and $maxCurveVal"
        }
}
