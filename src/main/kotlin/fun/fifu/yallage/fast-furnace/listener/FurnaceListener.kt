package `fun`.fifu.yallage.`fast-furnace`.listener

import `fun`.fifu.yallage.`fast-furnace`.Configuring
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Furnace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.*
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack


/**
 * Used to handle furnace related events
 */
class FurnaceListener : Listener {

    companion object {
        /**
         * This map is loaded with the coordinates and consumption status of the Fast Furnace
         */
        private val fastFurnaceMap = hashMapOf<Triple<Int, Int, Int>, Int>()

        /**
         * Read Fast Furnace number from location
         * @param t the location
         * @return frequency
         */
        fun readFastFurnace(t: Triple<Int, Int, Int>): Int {
            return fastFurnaceMap[t]?.minus(1) ?: 0
        }

        /**
         * Create a Fast Furnace for location,
         * @param t the location
         * @param frequency Fast Furnace 's frequency
         */
        fun createFastFurnace(t: Triple<Int, Int, Int>, frequency: Int) {
            fastFurnaceMap[t] = frequency + 1
        }

        /**
         * Expend one Fast Furnace from location
         * @param t will location expend
         */
        fun expendFastFurnace(t: Triple<Int, Int, Int>) {
            fastFurnaceMap[t] = fastFurnaceMap[t]?.minus(1) ?: 0
        }

        /**
         * Remove one Fast Furnace from location
         * @param t location t will remove
         */
        fun removeFastFurnace(t: Triple<Int, Int, Int>) {
            fastFurnaceMap.remove(t)
        }

        /**
         * Return if the furnace is a  Fast Furnace
         * @return true or false
         */
        private fun Block.isFastFurnace() = fastFurnaceMap.contains(Triple(x, y, z))

        /**
         * Return if the item stack is a Fast Furnace
         * @return true or false
         */
        private fun ItemStack.isFastFurnace() =
            type == Material.FURNACE && itemMeta?.lore?.get(0)?.contains(Configuring.configz.lore_1) == true

        /**
         * Returns the durability of the Fast Furnace
         * @return durability
         */
        private fun ItemStack.getTheDurability(): Int {
            if (!isFastFurnace())
                throw RuntimeException("The type being operated is not a Fast Furnace")
            return itemMeta!!.lore!![1]!!.readLore2Int()
        }

        /**
         * Set the durability of the Fast Furnace
         * @param dur can use number
         */
        private fun ItemStack.setTheDurability(dur: Int) {
            if (!isFastFurnace())
                throw RuntimeException("The type being operated is not a Fast Furnace")
            itemMeta?.lore?.set(1, dur.makeLore2String())
        }

        /**
         * Returns the durability of the Fast Furnace
         * @return durability
         */
        private fun Block.getTheDurability(): Int? {
            if (!isFastFurnace())
                throw RuntimeException("The type being operated is not a Fast Furnace")
            return fastFurnaceMap[Triple(x, y, z)]
        }

        /**
         * Set the durability of the Fast Furnace
         * @param dur can use number
         */
        private fun Block.setTheDurability(dur: Int) {
            if (!isFastFurnace())
                throw RuntimeException("The type being operated is not a Fast Furnace")
            fastFurnaceMap[Triple(x, y, z)] = dur
        }

        /**
         * Make Block's Location to Triple
         */
        private fun Block.toTriple() = Triple(x, y, z)

        /**
         * Set the durability -1 of the Fast Furnace
         */
        fun Block.decrease() {
            val dur = getTheDurability()!! - 1
            if (dur > 0) {
                setTheDurability(dur)
                location.world?.playSound(location, Configuring.configz.sound_use, 16.0f, 16.0f)
            } else {
                setTheDurability(0)
                fastFurnaceMap.remove(Triple(x, y, z))
                location.world?.playSound(location, Configuring.configz.sound_break, 16.0f, 16.0f)
                type = Material.AIR
            }
        }

        /**
         * Drop the block
         * @param itemStack blocks to drop
         */
        private fun Block.Drop(itemStack: ItemStack) {
            type = Material.AIR
            world.dropItem(location, itemStack)
        }

        /**
         * Echo the Furnace's Properties and Fields
         * @return Formatted Text
         */
        private fun Furnace.Echo(): String {
            return """
            customName=$customName
            cookTime = $cookTime
            cookTimeTotal = $cookTimeTotal
            burnTime = $burnTime
         """.trimIndent()
        }

        /**
         * Get Fast Furnace 's Item Stack
         * @return Fast Furnace Item Stack
         */
        fun getFastFurnace(): ItemStack {
            val itemStack = ItemStack(Material.FURNACE)
            val im = itemStack.itemMeta
            im!!.setDisplayName(
                Configuring.configz.display_name.replace(
                    "{can_use_number}",
                    Configuring.configz.can_use_number.toString()
                )
            )
            im.lore = arrayListOf(Configuring.configz.lore_1, Configuring.configz.can_use_number.makeLore2String())
            itemStack.itemMeta = im
            return itemStack
        }

        /**
         * Read Int from lore2
         * @return can use number
         */
        fun String.readLore2Int() = replace(Configuring.configz.lore_2_prefix, "").toInt()

        /**
         * Make lore_2 with can_use_number
         * @return lore_2 Text
         */
        fun Int.makeLore2String() = Configuring.configz.lore_2_prefix + this

    }

//    @EventHandler
//    fun onPlayerSay(event: AsyncPlayerChatEvent) {
//        event.player.sendMessage(event.message)
//        if (event.message.contains("快速熔炉")) {
//            event.player.inventory.addItem(getFastFurnace())
//            event.player.sendMessage("已给予 【快速熔炉】")
//        }
//    }

    @EventHandler
    fun onInvOpen(event: InventoryOpenEvent) {
        if (event.inventory.location?.block?.isFastFurnace() == true) {
            val t = event.inventory.location?.block!!.toTriple()
        }
    }

    @EventHandler
    fun onInvClick(event: InventoryClickEvent) {
        if (event.inventory.location!!.block.isFastFurnace() && event.slot <= 2) {
            val furnaceInventory = event.inventory as FurnaceInventory
            val holder = furnaceInventory.holder!!
            holder.cookTime = 199
            holder.update(true, true)
        }
    }

    @EventHandler
    fun onFurnaceSmelt(event: FurnaceSmeltEvent) {
        if (event.block.isFastFurnace()) {
            event.block.decrease()
        }
    }

    @EventHandler
    fun onFastFurnacePlace(event: BlockPlaceEvent) {
        if (event.itemInHand.isFastFurnace()) {
            createFastFurnace(event.block.toTriple(), event.itemInHand.getTheDurability())
            val furnace = event.block.state as Furnace
            furnace.customName = Configuring.configz.custom_name
            furnace.burnTime = Short.MAX_VALUE
            furnace.update()
        }
    }

    @EventHandler
    fun onFastFurnaceBreak(event: BlockBreakEvent) {
        val block = event.block
        if (block.isFastFurnace()) {
            val t = block.toTriple()
            val itemStack = ItemStack(Material.FURNACE)
            val im = itemStack.itemMeta
            im!!.lore = arrayListOf(Configuring.configz.lore_1, readFastFurnace(t).makeLore2String())
            im.setDisplayName(
                Configuring.configz.display_name.replace(
                    "{can_use_number}",
                    readFastFurnace(t).toString()
                )
            )
            itemStack.itemMeta = im
            removeFastFurnace(block.toTriple())
            block.Drop(itemStack)
        }
    }

}