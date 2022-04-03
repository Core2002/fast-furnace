package `fun`.fifu.yallage.`fast-furnace`.listener

import `fun`.fifu.yallage.`fast-furnace`.Configuring
import `fun`.fifu.yallage.`fast-furnace`.FastFurnace
import `fun`.fifu.yallage.`fast-furnace`.listener.FurnaceListener.Companion.isFastFurnace
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Furnace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.FurnaceSmeltEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable


/**
 * Used to handle furnace related events
 */
class FurnaceListener : Listener {

    companion object {
        /**
         * This map is loaded with the coordinates and consumption status of the Fast Furnace
         */
        val fastFurnaceMap = hashMapOf<Triple<Int, Int, Int>, Int>()
        val armorStandMap = hashMapOf<Triple<Int, Int, Int>, ArmorStand>()

        /**
         * Read Fast Furnace number from location
         * @param t the location
         * @return frequency
         */
        fun readFastFurnace(t: Triple<Int, Int, Int>): Int {
            return fastFurnaceMap[t] ?: 0
        }

        /**
         * Read Fast Furnace number from location for Player
         * @param t the location
         * @return frequency
         */
        fun readFastFurnace_Player(t: Triple<Int, Int, Int>): Int {
            return fastFurnaceMap[t]?.minus(1) ?: 0
        }

        /**
         * Create a Fast Furnace for location,
         * @param t the location
         * @param frequency Fast Furnace 's frequency
         */
        fun createFastFurnace(t: Triple<Int, Int, Int>, frequency: Int) {
            fastFurnaceMap[t] = frequency.plus(1)
        }

        /**
         * Expend one Fast Furnace from location
         * @param t will location expend
         */
        fun expendFastFurnace(t: Triple<Int, Int, Int>) {
            fastFurnaceMap[t] = fastFurnaceMap[t]!!.minus(1)
        }

        /**
         * Remove one Fast Furnace from location
         * @param t location t will remove
         */
        fun removeFastFurnace(t: Triple<Int, Int, Int>) {
            fastFurnaceMap.remove(t)
            armorStandMap[t]?.remove()
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
        private fun Block.getTheDurability(): Int {
            if (!isFastFurnace())
                throw RuntimeException("The type being operated is not a Fast Furnace")
            return readFastFurnace(Triple(x, y, z))
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
            expendFastFurnace(toTriple())
            if (getTheDurability() > 0) {
                location.world?.playSound(location, Configuring.configz.sound_use, 16.0f, 16.0f)
            } else {
                val furnace = state as Furnace
                furnace.inventory.smelting?.let { world.dropItem(location, it) }
                furnace.inventory.result?.let { world.dropItem(location, it) }
                furnace.inventory.fuel?.let { world.dropItem(location, it) }
                type = Material.AIR
                removeFastFurnace(Triple(x, y, z))
                location.world?.playSound(location, Configuring.configz.sound_break, 16.0f, 16.0f)
            }
        }

        /**
         * Drop the block
         * @param itemStack blocks to drop
         */
        private fun Block.Drop(itemStack: ItemStack) {
            type = Material.AIR
            if (itemStack.getTheDurability() > 0)
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

        /**
         * Make ArmorStand with Block
         * @return ArmorStand Object
         */
        fun Block.makeArmorStand(): ArmorStand {
            val t = toTriple()
            val armorStand = world.spawn(
                Location(world, t.first + 0.5, t.second + 1.25, t.third + 0.5),
                ArmorStand::class.java
            )
            armorStand.isCustomNameVisible = true
            armorStand.isVisible = false
            armorStand.setGravity(false)
            armorStand.customName = t.getTitleText()
            armorStand.isMarker = true
            armorStandMap[t]?.remove()
            armorStandMap[t] = armorStand
            return armorStand
        }

        /**
         * Get CustomName with Triple
         * @return Custom Name
         */
        fun Triple<Int, Int, Int>.getTitleText(): String {
            return Configuring.configz.show_title_text
                .replace("{x}", first.toString())
                .replace("{y}", (second - 1).toString())
                .replace("{z}", third.toString())
                .replace("{can_use_number}", readFastFurnace_Player(this).toString())
        }

        /**
         * ReFlash the Title from location
         */
        var tag = mutableMapOf<Triple<Int, Int, Int>, Int>()
        fun Block.ReFlash() {
            val t = toTriple()
            if (readFastFurnace(t) <= 0)
                removeFastFurnace(t)
            armorStandMap[t]?.customName = t.getTitleText()
            tag[t] = tag[t] ?: 0
            if (readFastFurnace(t) - 1 == 0) {
                if (tag[t]!! > 10) {
                    tag[t] = 0
                    removeFastFurnace(t)
                    val furnace = state as Furnace
                    furnace.inventory.smelting?.let { world.dropItem(location, it) }
                    furnace.inventory.result?.let { world.dropItem(location, it) }
                    furnace.inventory.fuel?.let { world.dropItem(location, it) }
                    type = Material.AIR
                } else {
                    tag[t] = tag[t]!!.plus(1)
                }
            }
        }

        init {
            FastFurnace.bigLoop = object : BukkitRunnable() {
                override fun run() {
                    try {
                        Bukkit.getWorlds().forEach {
                            fastFurnaceMap.forEach { (t, _) ->
                                val block = it.getBlockAt(t.first, t.second, t.third)
                                if (block.isFastFurnace()) {
                                    val furnaceInventory = (block.state as Furnace).inventory
                                    if (furnaceInventory.smelting != null) {
                                        val holder = furnaceInventory.holder!!
                                        if (Configuring.configz.crafting_table_mode) {
                                            if (furnaceInventory.smelting != null && furnaceInventory.smelting!!.type != Material.AIR) {
                                                furnaceInventory.result =
                                                    Configuring.configz.synthesisMapping[furnaceInventory.smelting?.type?.name]?.let { s ->
                                                        Material.getMaterial(s)?.let {
                                                            val itemStack = ItemStack(it)
                                                            itemStack.amount =
                                                                furnaceInventory.smelting!!.amount + (furnaceInventory.result?.amount
                                                                    ?: 0)
                                                            furnaceInventory.smelting = null
                                                            itemStack
                                                        }
                                                    }
                                                holder.update(true, true)
                                            }
                                        } else {
                                            holder.cookTime = 199
                                            holder.update(true, true)
                                        }

                                    }
                                }
                                block.ReFlash()
                            }
                        }
                    } catch (e: Exception) {
                    }
                }
            }.runTaskTimer(FastFurnace.plugin, 20, 1)
        }
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
            val furnaceInventory = event.inventory as FurnaceInventory
            furnaceInventory.fuel = ItemStack(Material.BARRIER)
        }
    }
//
//    @EventHandler
//    fun onInvClose(event: InventoryCloseEvent) {
//        val t = event.inventory.location?.block!!.toTriple()
//        if (readFastFurnace(t) == 0 && event.inventory.location?.block?.isFastFurnace() == true) {
//            event.inventory.location?.block!!.state.update()
//        }
//    }

    @EventHandler
    fun onInvClick(event: InventoryClickEvent) {
        if (Configuring.configz.crafting_table_mode) {
            if (event.inventory.location!!.block.isFastFurnace() && event.slotType==InventoryType.SlotType.FUEL) {
                val furnaceInventory = event.inventory as FurnaceInventory
                if (furnaceInventory.fuel?.type == Material.BARRIER)
                    event.isCancelled = true
            }
            if (event.inventory.location!!.block.isFastFurnace() && event.slot == 0) {
                val furnaceInventory = event.inventory as FurnaceInventory
                if (furnaceInventory.smelting != null && furnaceInventory.smelting!!.type != Material.AIR) {
                    furnaceInventory.result =
                        Configuring.configz.synthesisMapping[furnaceInventory.smelting?.type?.name]?.let { s ->
                            Material.getMaterial(s)?.let {
                                val itemStack = ItemStack(it)
                                itemStack.amount =
                                    furnaceInventory.smelting!!.amount + (furnaceInventory.result?.amount ?: 0)
                                furnaceInventory.smelting = null
                                itemStack
                            }
                        }
                }
            }
        } else {
            if (event.inventory.location!!.block.isFastFurnace() && event.slot <= 2) {
                val furnaceInventory = event.inventory as FurnaceInventory
                val holder = furnaceInventory.holder!!
                holder.cookTime = 199
                holder.update(true, true)
            }
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
            val t = event.blockPlaced.toTriple()
            createFastFurnace(event.blockPlaced.toTriple(), event.itemInHand.getTheDurability())
            val furnace = event.blockPlaced.state as Furnace
            furnace.customName = Configuring.configz.custom_name
            furnace.burnTime = Short.MAX_VALUE
            furnace.update()
            furnace.block.makeArmorStand()
            furnace.block.ReFlash()
        }
    }

    @EventHandler
    fun onFastFurnaceBreak(event: BlockBreakEvent) {
        val block = event.block
        if (block.isFastFurnace()) {
            val t = block.toTriple()
            val itemStack = ItemStack(Material.FURNACE)
            val im = itemStack.itemMeta
            im!!.lore = arrayListOf(Configuring.configz.lore_1, readFastFurnace_Player(t).makeLore2String())
            im.setDisplayName(
                Configuring.configz.display_name.replace(
                    "{can_use_number}",
                    readFastFurnace_Player(t).toString()
                )
            )
            im.addEnchant(
                Enchantment.DURABILITY,
                if (readFastFurnace_Player(t) > 10) 10 else readFastFurnace_Player(t),
                true
            )
            itemStack.itemMeta = im
            val furnace = block.state as Furnace
            furnace.inventory.smelting?.let { block.world.dropItem(block.location, it) }
            furnace.inventory.result?.let { block.world.dropItem(block.location, it) }
            furnace.inventory.fuel?.let { block.world.dropItem(block.location, it) }
            removeFastFurnace(block.toTriple())
            block.Drop(itemStack)
            block.ReFlash()
            furnace.type = Material.AIR
        }
    }

    @EventHandler
    fun onFastFurnaceExplode(event: EntityExplodeEvent) {
        event.blockList().forEach {
            if (it.isFastFurnace()) {
                val block = it
                if (block.isFastFurnace()) {
                    val t = block.toTriple()
                    val itemStack = ItemStack(Material.FURNACE)
                    val im = itemStack.itemMeta
                    im!!.lore = arrayListOf(Configuring.configz.lore_1, readFastFurnace_Player(t).makeLore2String())
                    im.setDisplayName(
                        Configuring.configz.display_name.replace(
                            "{can_use_number}",
                            readFastFurnace_Player(t).toString()
                        )
                    )
                    im.addEnchant(
                        Enchantment.DURABILITY,
                        if (readFastFurnace_Player(t) > 10) 10 else readFastFurnace_Player(t),
                        true
                    )
                    itemStack.itemMeta = im
                    val furnace = block.state as Furnace
                    furnace.inventory.smelting?.let { block.world.dropItem(block.location, it) }
                    furnace.inventory.result?.let { block.world.dropItem(block.location, it) }
                    furnace.inventory.fuel?.let { block.world.dropItem(block.location, it) }
                    removeFastFurnace(block.toTriple())
                    block.Drop(itemStack)
                    block.ReFlash()
                    furnace.type = Material.AIR
                }
            }
        }
    }

}