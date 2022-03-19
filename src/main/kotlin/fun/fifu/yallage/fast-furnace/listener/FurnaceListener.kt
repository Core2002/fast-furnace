package `fun`.fifu.yallage.`fast-furnace`.listener

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Furnace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.*
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack


/**
 * Used to handle furnace related events
 */
class FurnaceListener : Listener {

    companion object {
        private val fastFurnaceMap = hashMapOf<Triple<Int, Int, Int>, Int>()

        fun readFastFurnace(t: Triple<Int, Int, Int>): Int {
            return fastFurnaceMap[t]?.minus(1) ?: 0
        }

        fun createFastFurnace(t: Triple<Int, Int, Int>, frequency: Int) {
            fastFurnaceMap[t] = frequency + 1
        }

        fun expendFastFurnace(t: Triple<Int, Int, Int>) {
            fastFurnaceMap[t] = fastFurnaceMap[t]?.minus(1) ?: 0
        }

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
            type == Material.FURNACE && itemMeta?.lore?.get(0)?.contains("快速熔炉") == true

        /**
         * Returns the durability of the Fast Furnace
         * @return durability
         */
        private fun ItemStack.getTheDurability(): Int {
            if (!isFastFurnace())
                throw RuntimeException("The type being operated is not a Fast Furnace")
            return itemMeta!!.lore!![1]!!.toInt()
        }

        /**
         * Set the durability of the Fast Furnace
         */
        private fun ItemStack.setTheDurability(dur: Int) {
            if (!isFastFurnace())
                throw RuntimeException("The type being operated is not a Fast Furnace")
            itemMeta?.lore?.set(1, dur.toString())
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
            if (dur > 0)
                setTheDurability(dur)
            else {
                setTheDurability(0)
                fastFurnaceMap.remove(Triple(x, y, z))
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

        private fun Furnace.Echo(): String {
            return """
            customName=$customName
            cookTime = $cookTime
            cookTimeTotal = $cookTimeTotal
            burnTime = $burnTime
         """.trimIndent()
        }
    }

    @EventHandler
    fun onPlayerSay(event: AsyncPlayerChatEvent) {
        event.player.sendMessage(event.message)
        if (event.message.contains("快速熔炉")) {
            val itemStack = ItemStack(Material.FURNACE)
            val im = itemStack.itemMeta
            im!!.lore = arrayListOf("快速熔炉", "4")
            itemStack.itemMeta = im
            event.player.inventory.addItem(itemStack)
            event.player.sendMessage("已给予 【快速熔炉】")
        }
    }

    @EventHandler
    fun onInvOpen(event: InventoryOpenEvent) {
        if (event.inventory.location?.block?.isFastFurnace() == true) {
            val t = event.inventory.location?.block!!.toTriple()
            event.player.sendMessage("你打开了${t}的快速熔炉，他的剩余次数是${readFastFurnace(t)}")
        }
    }

    @EventHandler
    fun onInvClick(event: InventoryClickEvent) {
        if (event.inventory.location!!.block.isFastFurnace() && event.slot <= 2) {
            val furnaceInventory = event.inventory as FurnaceInventory
            val holder = furnaceInventory.holder!!
            holder.cookTime = 199
            holder.update(true, true)
            println(
                """
                slot = ${event.slot}
                slotType = ${event.slotType}
                result = ${furnaceInventory.result}
                smelting = ${furnaceInventory.smelting}
            """.trimIndent()
            )
            event.whoClicked.sendMessage(furnaceInventory.holder!!.Echo())
        }
    }

    /**
     * 反应结束
     */
    @EventHandler
    fun onFurnaceSmelt(event: FurnaceSmeltEvent) {
        if (event.block.isFastFurnace()) {
            event.block.decrease()
            println("触发  反应结束")
        }
    }

    /**
     * 取出产物
     */
    @EventHandler
    fun onFurnaceExtract(event: FurnaceExtractEvent) {
        if (event.block.isFastFurnace()) {
            println("触发  取出产物")
//            val furnace = event.block.state as Furnace
//
//            furnace.update()
            println("${event.block.toTriple()}的快速熔炉，消费了一次，他的剩余次数是${readFastFurnace(event.block.toTriple())}")
        }
    }

    @EventHandler
    fun onFastFurnacePlace(event: BlockPlaceEvent) {
        if (event.itemInHand.isFastFurnace()) {
            createFastFurnace(event.block.toTriple(), event.itemInHand.getTheDurability())
            val furnace = event.block.state as Furnace
            furnace.customName = "快速熔炉"
            furnace.burnTime = Short.MAX_VALUE
            furnace.update()
            event.player.sendMessage("你在${event.block.toTriple()}放置了一个快速熔炉，他的剩余次数是${readFastFurnace(event.block.toTriple())}")
        }
    }

    @EventHandler
    fun onFastFurnaceBreak(event: BlockBreakEvent) {
        val block = event.block
        if (block.isFastFurnace()) {
            val t = block.toTriple()
            val itemStack = ItemStack(Material.FURNACE)
            val im = itemStack.itemMeta
            im!!.lore = arrayListOf("快速熔炉", "${readFastFurnace(t)}")
            itemStack.itemMeta = im
            event.player.sendMessage("你在${t}拆除了一个快速熔炉，他的剩余次数是${readFastFurnace(t)}")
            removeFastFurnace(block.toTriple())
            block.Drop(itemStack)
        }
    }

}