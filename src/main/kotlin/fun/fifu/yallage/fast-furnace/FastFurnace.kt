package `fun`.fifu.yallage.`fast-furnace`

import `fun`.fifu.yallage.`fast-furnace`.command.FastFurnaceCommand
import `fun`.fifu.yallage.`fast-furnace`.listener.FurnaceListener
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class FastFurnace : JavaPlugin() {
    companion object {
        lateinit var plugin: FastFurnace
    }

    override fun onLoad() {
        Configuring.loadConfig()
    }

    override fun onEnable() {
        plugin = this
        server.pluginManager.registerEvents(FurnaceListener(), this)
        Bukkit.getPluginCommand("fast-furnace")?.setExecutor(FastFurnaceCommand())
        logger.info("Fast Furnace plugin is Loaded. By:NekokeCore")
    }

    override fun onDisable() {
        logger.info("Fast Furnace plugin is Disabled. Thinks for use :)")
    }
}