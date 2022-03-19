package `fun`.fifu.yallage.`fast-furnace`.pojo

import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class Config {
    @SerializedName("can_use_number")
    var can_use_number = 4
}
