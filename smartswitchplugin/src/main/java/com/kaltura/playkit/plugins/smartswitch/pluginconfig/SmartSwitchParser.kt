package com.kaltura.playkit.plugins.smartswitch.pluginconfig

import androidx.annotation.Nullable
import com.google.gson.annotations.SerializedName

class SmartSwitchParser {
    @Nullable
    var providers: ArrayList<Provider>? = null
    @Nullable
    var cdnBalanceMethod: String? = null
    @Nullable
    var UUID: String? = null
}

class Provider {
    @Nullable
    @SerializedName("provider")
    var provider: String? = null
    @Nullable
    @SerializedName("name")
    var name: String? = null
    @Nullable
    @SerializedName("url")
    var url: String? = null
    @Nullable
    @SerializedName("score")
    var score: Float? = null
}

class SmartSwitchErrorResponse {
    var messages: ArrayList<ErrorResponseBody>? = null
    // var data: Array? = null
}

class ErrorResponseBody {
    var type: String? = null
    var code: String? = null
    var message: String? = null
    // var parameters: Array? = null
}
