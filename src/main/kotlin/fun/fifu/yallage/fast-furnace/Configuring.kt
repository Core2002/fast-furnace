package `fun`.fifu.yallage.`fast-furnace`

import com.google.gson.Gson
import `fun`.fifu.yallage.`fast-furnace`.pojo.Config
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.io.*
import java.lang.reflect.Field

object Configuring {
    const val pluginName = "FastFurnace"

    object SlimeLogger {
        var logger = Bukkit.getLogger()
        fun info(message: String) {
            logger.info(ChatColor.GREEN.toString() + "[$pluginName] " + message)
        }

        fun warning(message: String) {
            logger.warning(ChatColor.YELLOW.toString() + "[$pluginName] " + message)
        }

        fun severe(message: String) {
            logger.severe(ChatColor.RED.toString() + "[$pluginName] " + message)
        }
    }

    var gson = Gson()
    var configz = Config()

    fun loadConfig() {
        Bukkit.getPluginManager().getPlugin(pluginName)?.saveResource("config.json", false)

        // 读取配置文件
        try {
            val file = File("plugins/$pluginName/config.json")
            val reader = BufferedReader(FileReader(file, Charsets.UTF_8))
            SlimeLogger.info("配置文件加载中...")
            configz = gson.fromJson(reader, Config::class.java)
            // 反射检查服务器配置任意变量是否为空
            val fields: Array<Field> = configz.javaClass.declaredFields
            for (field in fields) {
                field.isAccessible = true
                if (field[configz] == null) {
                    SlimeLogger.severe("错误的配置 " + field.name)
                    Bukkit.getServer().shutdown()
                }
            }
            SlimeLogger.info("配置文件加载完成")
        } catch (exception: FileNotFoundException) {
            SlimeLogger.severe("config.json 文件未找到.")
        } catch (exception: IllegalAccessException) {
            SlimeLogger.severe("配置文件非法参数")
        }
    }

    fun saveConfig() {
        val file = File("config.json")
        val json = gson.toJson(configz)
        try {
            val outputStream: OutputStream = FileOutputStream(file)
            outputStream.write(json.toByteArray())
            SlimeLogger.info("配置文件已保存")
        } catch (exception: IOException) {
            exception.message?.let { SlimeLogger.severe(it) }
        }
    }

    fun reloadConfig() {
        loadConfig()
        SlimeLogger.info("重新加载配置文件")
    }
}