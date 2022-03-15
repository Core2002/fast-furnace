package `fun`.fifu.yallage.`fast-furnace`.listener

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Furnace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack

/**
 * Used to handle furnace related events
 */
class FurnaceListener : Listener {
    var fastFurnaceMap = hashMapOf<Triple<Int, Int, Int>, Int>()

    @EventHandler
    fun onPlayerSay(event: PlayerChatEvent) {
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
        val inventory = event.inventory
        if (inventory is FurnaceInventory && inventory.holder!!.isFastFurnace()) {
            event.player.closeInventory()
            val createInventory = Bukkit.createInventory(event.inventory.holder, InventoryType.FURNACE, "快速熔炉")
            event.player.openInventory(createInventory)
        }
    }

    @EventHandler
    fun onInvClick(event: InventoryClickEvent) {
        if (event.inventory.holder is Furnace && (event.inventory.holder as Furnace).isFastFurnace()) {
            val holder = event.inventory.holder as Furnace
            if (holder.cookTimeTotal != 0) {
                holder.cookTimeTotal = 0
                holder.decrease()
            }
        }
    }


    @EventHandler
    fun onFastFurnacePlace(event: BlockPlaceEvent) {
        if (event.block is Furnace && event.itemInHand.isFastFurnace()) {
            fastFurnaceMap[event.block.toTriple()] = event.itemInHand.getTheDurability()
        }
    }

    @EventHandler
    fun onFastFurnaceBreak(event: BlockBreakEvent) {
        val block = event.block
        if (block is Furnace && block.isFastFurnace()) {
            block.getTheDurability()?.let { event.player.inventory.itemInMainHand.setTheDurability(it) }
        }
    }

    /**
     * Return if the furnace is a  Fast Furnace
     * @return true or false
     */
    fun Furnace.isFastFurnace() = fastFurnaceMap.contains(Triple(x, y, z))


    /**
     * Return if the item stack is a Fast Furnace
     * @return true or false
     */
    fun ItemStack.isFastFurnace() = type != Material.FURNACE && itemMeta?.lore?.get(0) == "快速熔炉"

    /**
     * Returns the durability of the Fast Furnace
     * @return durability
     */
    fun ItemStack.getTheDurability(): Int {
        if (!isFastFurnace())
            throw RuntimeException("The type being operated is not a Fast Furnace")
        return itemMeta!!.lore!![1]!!.toInt()
    }

    /**
     * Set the durability of the Fast Furnace
     */
    fun ItemStack.setTheDurability(dur: Int) {
        if (!isFastFurnace())
            throw RuntimeException("The type being operated is not a Fast Furnace")
        itemMeta?.lore?.set(1, dur.toString())
    }

    /**
     * Returns the durability of the Fast Furnace
     * @return durability
     */
    fun Furnace.getTheDurability(): Int? {
        if (!isFastFurnace())
            throw RuntimeException("The type being operated is not a Fast Furnace")
        return fastFurnaceMap[Triple(x, y, z)]
    }

    /**
     * Set the durability of the Fast Furnace
     */
    fun Furnace.setTheDurability(dur: Int) {
        if (!isFastFurnace())
            throw RuntimeException("The type being operated is not a Fast Furnace")
        fastFurnaceMap[Triple(x, y, z)] = dur
    }

    /**
     * Make Block's Location to Triple
     */
    fun Block.toTriple() = Triple(x, y, z)

    /**
     * Set the durability -1 of the Fast Furnace
     */
    fun Furnace.decrease() {
        val dur = getTheDurability()!! - 1
        if (dur > 0)
            setTheDurability(dur)
        else {
            setTheDurability(0)
            fastFurnaceMap.remove(Triple(x, y, z))
            block.type = Material.AIR
        }
    }
}