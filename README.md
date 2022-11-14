[![CI Status](https://travis-ci.org/kaltura/playkit-android-smartSwitch.svg?branch=develop)](https://travis-ci.org/kaltura/playkit-android-smartSwitch)
[![Download](https://img.shields.io/maven-central/v/com.kaltura.playkit/smartswitchplugin?label=Download)](https://search.maven.org/artifact/com.kaltura.playkit/smartswitchplugin)
[![License](https://img.shields.io/badge/license-AGPLv3-black.svg)](https://github.com/kaltura/playkit-android-smartSwitch/blob/develop/LICENSE)
![Android](https://img.shields.io/badge/platform-android-green.svg)

# playkit-android-smartSwitch
Kaltura Player plugin for NPAW Smart Switch


 This plugin is built upon Kaltura Player.
 In case player issue an API call towards the Youbora smart-switch service, which will return an ordered list of CDNs.
 The plugin will pick the first one and use the url inside for the playback.

  In case of error the original url that was retrieved from the the BE will be used and a non-fatal  SMART_SWITCH Error will be fired to the 
  app 

#### Plugin Config
```kotlin
// SmartSwitch Configuration
val pkPluginConfigs = PKPluginConfigs()
val optionalParams: HashMap<String, String> = HashMap()
optionalParams.put("OPTION_PARAM_KEY_1", "OPTION_PARAM_VALUE_1")
optionalParams.put("OPTION_PARAM_KEY_2", "OPTION_PARAM_VALUE_2")

val smartSwitchConfig = SmartSwitchConfig("YOUR_ACCOUNT_CODE", "YOUR_ORIGIN_CODE", optionalParams)
pkPluginConfigs.setPluginConfig(SmartSwitchPlugin.factory.name, smartSwitchConfig)

playerInitOptions.setPluginConfigs(pkPluginConfigs)

#create player with the playerInitOptions
player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)

```

```kotlin
/** * Create SmartSwitch Config object
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
```

#### Error Listener

```kotlin
  player?.addListener(this, SmartSwitchEvent.error) { event ->
            Log.i(TAG, "SmartSwitch ERROR " + event.errorMessage)
        }

```




