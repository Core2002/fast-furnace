package `fun`.fifu.yallage.`fast-furnace`.pojo

import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class Config {
    @SerializedName("can_use_number")
    val can_use_number = 4

    @SerializedName("display_name")
    val display_name = "§a快速熔炉 剩余{can_use_number}次"

    @SerializedName("custom_name")
    val custom_name = "§a快速熔炉 剩余{can_use_number}次"

    @SerializedName("lore_1")
    val lore_1 = "这是一只快速熔炉"

    @SerializedName("lore_2_prefix")
    val lore_2_prefix = "§e"

    @SerializedName("sound_use")
    val sound_use = "entity.wolf.pant"

    @SerializedName("sound_break")
    val sound_break = "entity.wolf.hurt"

    @SerializedName("show_title_text")
    val show_title_text = "§4快速熔炉 剩余{can_use_number}次"
}
