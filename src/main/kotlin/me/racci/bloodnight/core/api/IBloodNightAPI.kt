package me.racci.bloodnight.core.api

import org.bukkit.World

/**
 * @since 0.8
 */
interface IBloodNightAPI {
    /**
     * Checks if a blood night is active.
     *
     * @param world world
     *
     * @return true if a blood night is active.
     */
    fun isBloodNightActive(world: World): Boolean

    /**
     * Force the next night to be a blood night in a world.
     *
     *
     * This will not set the time in the world.
     *
     * @param world world
     */
    fun forceNight(world: World)

    /**
     * Cancels a blood night a world if one is active.
     *
     * @param world world
     */
    fun cancelNight(world: World)

    /**
     * Get all worlds where a blood night is currently active.
     *
     * @return set of worlds.
     */
    val bloodWorlds: Set<World>

    /**
     * Returns how many seconds of the blood night are left.
     *
     * @param world the world to check
     *
     * @return the amount of seconds or 0 if not blood night is active.
     */
    fun getSecondsLeft(world: World): Int

    /**
     * Get the percent of blood night duration left.
     *
     *
     * The start is 100 and the end is 0.
     *
     *
     * If no blood night is active this method will always return 0.
     *
     * @param world the world to check
     *
     * @return the percent between 100 and 0.
     */
    fun getPercentLeft(world: World): Double

    /**
     * Get the probability of the next night to become a blood night.
     *
     *
     * Calling this function is equal to [.nextProbability] with offset 1;
     *
     * @param world world to check
     *
     * @return probability between 0 and 100. Where 100 is a guaranteed blood night.
     */
    fun nextProbability(world: World): Int {
        return nextProbability(world, 1)
    }

    /**
     * Get the probability of the next night to become a blood night.
     *
     * @param world  world to check
     * @param offset offset of nights. The next night has an offset of 1. The last night has an offset of 0.
     *
     * @return probability between 0 and 100. Where 100 is a guaranteed blood night.
     */
    fun nextProbability(world: World, offset: Int): Int
}