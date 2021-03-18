package com.kaltura.playkit.plugins.smartswitch.pluginconfig

import androidx.annotation.Nullable

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
    var CDN_NAME: String? = null
    @Nullable
    var CDN_CODE: String? = null
    @Nullable
    var URL: String? = null
    @Nullable
    var CDN_SCORE: Float? = null
}

class SmartSwitchErrorResponse {
    var messages: ArrayList<ErrorResponseBody>? = null
    // var data: Array? = null //TODO
}

class ErrorResponseBody {
    var type: String? = null
    var code: String? = null
    var message: String? = null
    // var parameters: Array? = null //TODO
}
