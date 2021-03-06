package com.kaltura.playkit.plugins.smartswitch.pluginconfig

import android.webkit.URLUtil
import androidx.annotation.NonNull
import java.util.HashMap

/**
 * Create SmartSwitch Config object
 *
 * @param accountCode Mandatory: YOUBORA account code
 * @param originCode Mandatory: CDN group configured to select a subset of configured CDNs
 * @param optionalParams Optional:
 *                          Ex. ip(String), extraData(Boolean), userAgent(String),
 *                          live(Boolean), nva(Not Valid After timestamp in UTC +1, Integer),
 *                          nvb(Not  Valid  Before  timestamp  in  UTC +1,Integer), token(String)
 * @param smartSwitchUrl Optional: SmartSwitch server url
 *
 * @return SmartSwitchConfig
 */
data class SmartSwitchConfig @JvmOverloads constructor(@NonNull val accountCode: String? = null,
                                                       @NonNull val originCode: String? = null,
                                                       val optionalParams: HashMap<String, String>? = null,
                                                       var smartSwitchUrl: String? = null) {
    init {
        if (smartSwitchUrl.isNullOrEmpty() || !URLUtil.isNetworkUrl(smartSwitchUrl)) {
            smartSwitchUrl = "http://cdnbalancer.youbora.com/orderedcdn"
        }
    }
}

