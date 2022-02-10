package com.kaltura.playkit.plugins.smartswitch.pluginconfig

import android.webkit.URLUtil
import androidx.annotation.NonNull
import java.util.HashMap

/**
 * Create SmartSwitch Config object
 *
 * @param accountCode Mandatory: YOUBORA account code
 * @param optionalParams Optional:
 *                          Ex:
 *                          originCode(String), CDN group configured to select subet of CDN's
 *                          ip(String),
 *                          userAgent(String),
 *                          live(Is live media ,"true"/"false"),
 *                          nva(Not Valid After timestamp in UTC +1, Integer),
 *                          nvb(Not Valid Before timestamp in UTC +1, Integer),
 *                          secretKey:(API secret that can be configured from the UI, String)
 *                          extended(will add score/UUID filed in response, "true"/"false")
 *                          token(Additional security that can be added in the request if an API key is enabled in the UI.
 *                                  This token should be generated by creating a MD5 hash using the following parameters
 *                                  token = MD5(accountCode + originCode + resource + nva + nvb + secretKey),String)
 *
 * @param application Optional: application name, default: "default"
 * @param domainUrl Optional: SmartSwitch domain url default: https://api.gbnpaw.com
 *
 * @return SmartSwitchConfig
 */
data class SmartSwitchConfig @JvmOverloads constructor(@NonNull val accountCode: String? = null,
                                                       val optionalParams: HashMap<String, String>? = null,
                                                       @NonNull val application: String? = "default",
                                                       var domainUrl: String? = null) {
    init {
        var apiPathParams = "${accountCode}/${application}/decision"
        if (domainUrl.isNullOrEmpty() || !URLUtil.isValidUrl(domainUrl)) {
            domainUrl = "https://api.gbnpaw.com/$apiPathParams"
        } else {
            domainUrl = "$domainUrl/$apiPathParams"
        }
    }
}

