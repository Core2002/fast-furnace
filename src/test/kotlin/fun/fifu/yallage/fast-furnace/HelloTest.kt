package `fun`.fifu.yallage.`fast-furnace`

import org.junit.Test

class HelloTest {
    @Test
    fun lore_2_prefixTest() {
        var lore_2 = ""

        val lore_2_prefix = "§e剩余次数："
        val can_use_number = 4

        fun change(number: Int) {
            lore_2 = lore_2_prefix + number
        }

        fun read(): Int {
            return lore_2.replace(lore_2_prefix, "").toInt()
        }

        lore_2 = lore_2_prefix + can_use_number

        println("lore_2 = $lore_2 read it = ${read()}")
        change(666)
        println("lore_2 = $lore_2 read it = ${read()}")


    }
}
