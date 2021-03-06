[![CI Status](https://travis-ci.org/kaltura/playkit-android-smartSwitch.svg?branch=develop)](https://travis-ci.org/kaltura/playkit-android-smartSwitch)
[![Download](https://img.shields.io/maven-central/v/com.kaltura.playkit/smartswitchplugin?label=Download)](https://search.maven.org/artifact/com.kaltura.playkit/smartswitchplugin)
[![License](https://img.shields.io/badge/license-AGPLv3-black.svg)](https://github.com/kaltura/playkit-android-smartSwitch/blob/develop/LICENSE)
![Android](https://img.shields.io/badge/platform-android-green.svg)

# playkit-android-smartSwitch
Kaltura Player plugin for NPAW Smart Switch

This plugin can be used via KalturaPlayer Only and the following plugin configuration shoud be used:

```
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
```

