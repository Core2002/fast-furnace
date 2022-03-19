package `fun`.fifu.yallage.`fast-furnace`.command

import `fun`.fifu.yallage.`fast-furnace`.Configuring
import `fun`.fifu.yallage.`fast-furnace`.listener.FurnaceListener
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class FastFurnaceCommand : TabExecutor {

    private val helpMassage = mapOf(
        "get-item" to "/fast-furnace get-item <Player> <Number> 给玩家快速熔炉"
    )

    override fun onTabComplete(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): MutableList<String> {
        if (p0 !is Player) return mutableListOf()
        if (p3.size == 1) return helpMassage.keys.toMutableList()
        val ml = mutableListOf<String>()
        val playersName = mutableListOf<String>()
        Bukkit.getOnlinePlayers().forEach {
            playersName.add(it.name)
        }
        return when (p3[0]) {
            else -> ml
        }
    }


    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
//        println(
//            """
//            p0:$p0
//            p1:$p1
//            p2:$p2
//            p3:${p3.contentToString()}
//        """.trimIndent()
//        )

        if (p3.isEmpty()) return onHelp(p0, p3)
        try {
            val re = when (p3[0]) {
                "help" -> onHelp(p0, p3)
                "get-item" -> onGetItem(p0, p3)
                else -> false
            }
            if (!re) onHelp(p0, arrayOf("help", p3[0]))
        } catch (e: Exception) {
            onHelp(p0, arrayOf("help", p3[0]))
            Configuring.SlimeLogger.warning("$p0 的命令 /fast-furnace ${p3.contentToString()} 导致了一个异常：")
            e.printStackTrace()
            return true
        }
        return true

    }

    private fun onGetItem(p0: CommandSender, p3: Array<out String>): Boolean {
        if (!p0.hasPermission("fast-furnace.get-item")) {
            p0.sendMessage("你必须获得 fast-furnace.get-item 权限才能在游戏内使用此命令")
            return true
        }

        val fastFurnace = FurnaceListener.getFastFurnace()
        fastFurnace.amount = p3[2].toInt()
        Bukkit.getPlayer(p3[1])?.inventory!!.addItem(fastFurnace)
        return true
    }

    private fun onHelp(player: CommandSender, p3: Array<out String>): Boolean {
        if (p3.size > 1) {
            helpMassage[p3[1]]?.let { player.sendMessage(it) }
        } else {
            val sb = StringBuffer()
            helpMassage.values.forEach { sb.append(it).append("\n") }
            player.sendMessage("FastFurnace By. 小白 uid:30924239")
            player.sendMessage("帮助：/fast-furnace <命令>\n$sb")
            return true
        }
        return true
    }
}