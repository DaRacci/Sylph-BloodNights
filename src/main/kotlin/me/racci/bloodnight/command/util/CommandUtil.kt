package me.racci.bloodnight.command.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import java.util.*
import java.util.function.Function
import java.util.function.Predicate
import kotlin.math.floor

object CommandUtil {
    /**
     * Get a page from a collection. This collection will always be of size 0-size.
     *
     * @param collection collection to parse
     * @param page       page starting from 0
     * @param size       size of a page
     * @param <T>        Type of collection
     * @return collection with a size between 0 and size
    </T> */
    private fun <T> getSlice(collection: Collection<T>, page: Int, size: Int): Collection<T> {
        val list: List<T> = ArrayList(collection)
        val slice: MutableList<T> = ArrayList()
        var i = page * size
        while (i < page * size + size && i < collection.size) {
            slice.add(list[i])
            i++
        }
        return slice
    }

    private fun pageCount(collection: Collection<*>, size: Int): Int {
        return floor((collection.size - 1).coerceAtLeast(0) / size.toDouble()).toInt()
    }

    private fun getPageFooter(page: Int, pageMax: Int, pageCommand: String): TextComponent {
        val builder = Component.text()
        builder.append(Component.text("=====<| ", NamedTextColor.YELLOW))
        if (page != 0) {
            builder.append(
                Component.text("<<< ", NamedTextColor.AQUA)
                    .clickEvent(
                        ClickEvent.runCommand(pageCommand.replace("{page}", (page - 1).toString()))
                    )
            )
        } else {
            builder.append(
                Component.text("<<< ", NamedTextColor.GRAY)
            )
        }
        builder.append(Component.text((page + 1).toString() + "/" + (pageMax + 1), NamedTextColor.YELLOW))
        if (page != pageMax) {
            builder.append(
                Component.text(" >>>")
                    .clickEvent(
                        ClickEvent.runCommand(pageCommand.replace("{page}", (page + 1).toString()))
                    )
            ).color(NamedTextColor.AQUA)
        } else {
            builder.append(
                Component.text(" >>>", NamedTextColor.GRAY)
            )
        }
        builder.append(Component.text(" |>=====", NamedTextColor.YELLOW))
        return builder.build()
    }

    val footer: TextComponent
        get() = Component.text()
            .append(Component.text("=====<|    ", NamedTextColor.YELLOW))
            .append(Component.text("    |>=====", NamedTextColor.YELLOW))
            .build()

    fun <T> findPage(content: Collection<T>, pageSize: Int, predicate: Predicate<T>): OptionalInt {
        val iterator = content.iterator()
        var page = 0
        while (iterator.hasNext()) {
            for (i in 0 until pageSize) {
                if (!iterator.hasNext()) break
                val next = iterator.next()
                if (predicate.test(next)) return OptionalInt.of(page)
            }
            page++
        }
        return OptionalInt.empty()
    }

    fun <T> getPage(
        content: Collection<T>,
        page: Int,
        mapping: Function<T, TextComponent?>,
        title: String?,
        pageCommand: String
    ): TextComponent {
        return getPage(content, page, 18, 1, mapping, title, pageCommand)
    }

    fun <T> getPage(
        content: Collection<T>,
        page: Int,
        elementsPerPage: Int,
        elementSize: Int,
        mapping: Function<T, TextComponent?>,
        title: String?,
        pageCommand: String
    ): TextComponent {
        val elements = getSlice(content, page, elementsPerPage)
        val builder = Component.text()
        for (i in elements.size * elementSize..17) {
            builder.append(Component.newline())
        }
        builder.append(Component.text("=====<| ").color(NamedTextColor.YELLOW))
            .append(Component.text(title!!).color(NamedTextColor.AQUA))
            .append(Component.text(" |>=====").color(NamedTextColor.YELLOW))
        for (t in elements) {
            builder.append(Component.newline())
                .append(mapping.apply(t)!!)
        }
        return builder.append(Component.newline())
            .append(
                getPageFooter(
                    page,
                    pageCount(content, elementsPerPage),
                    pageCommand
                )
            ).build()
    }

    fun getHeader(title: String?): TextComponent {
        return Component.text()
            .append(Component.text("=====<| ").color(NamedTextColor.YELLOW))
            .append(Component.text(title!!).color(NamedTextColor.AQUA))
            .append(Component.text(" |>=====").color(NamedTextColor.YELLOW)).build()
    }

    fun getBooleanField(
        currValue: Boolean,
        cmd: String,
        field: String,
        positive: String?,
        negative: String?
    ): TextComponent {
        return Component.text()
            .append(Component.text("$field: ", NamedTextColor.AQUA))
            .append(
                Component.text(
                    positive!!,
                    if (currValue) NamedTextColor.GREEN else NamedTextColor.DARK_GRAY
                )
                    .clickEvent(
                        ClickEvent.runCommand(cmd.replace("{bool}", "true"))
                    )
            )
            .append(Component.space())
            .append(
                Component.text(
                    negative!!,
                    if (!currValue) NamedTextColor.RED else NamedTextColor.DARK_GRAY
                )
                    .clickEvent(
                        ClickEvent.runCommand(cmd.replace("{bool}", "false"))
                    )
            )
            .build()
    }

    fun getToggleField(currValue: Boolean, cmd: String, field: String): TextComponent {
        val newCmd = cmd.replace("{bool}", currValue.not().toString())
        return Component.text()
            .append(
                Component.text(
                    "[$field]",
                    if (currValue) NamedTextColor.GREEN else NamedTextColor.DARK_GRAY
                )
                    .clickEvent(
                        ClickEvent.runCommand(newCmd)
                    )
            )
            .build()
    }
}