package com.kaltura.playkit.plugins.smartswitch.pluginconfig

import java.util.HashMap

data class SmartSwitchConfig @JvmOverloads constructor(val accountCode: String? = null,
                                                       val originCode: String? = null,
                                                       val optionalParams: HashMap<String, String>? = null)