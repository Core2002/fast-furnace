package `fun`.fifu.yallage.`fast-furnace`

import `fun`.fifu.yallage.`fast-furnace`.listener.FurnaceListener
import org.bukkit.plugin.java.JavaPlugin

class FastFurnace :JavaPlugin(){
    override fun onEnable() {
        server.pluginManager.registerEvents(FurnaceListener(),this)
        logger.info("Fast Furnace plugin is Loaded. By:NekokeCore")
    }

    override fun onDisable() {
        logger.info("Fast Furnace plugin is Disabled. Thinks for use :)")
    }
}