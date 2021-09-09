package com.kaltura.playkit.plugins.smartswitch.pluginconfig

import androidx.annotation.Nullable
import com.google.gson.annotations.SerializedName

class SmartSwitchParser {
    @Nullable
    var smartSwitch: SmartSwitch? = null
}

class SmartSwitch {
    @Nullable
    var switchingMethod: String? = null
    @Nullable
    var CDNList: ArrayList<Map<String?, CDNList?>>? = null
    @Nullable
    var UUID: String? = null
}

class CDNList {
    @Nullable
    @SerializedName("CDN_NAME")
    var cdnName: String? = null
    @Nullable
    @SerializedName("CDN_CODE")
    var cdnCode: String? = null
    @Nullable
    @SerializedName("URL")
    var url: String? = null
    @Nullable
    @SerializedName("CDN_SCORE")
    var cdnScore: Float? = null
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
