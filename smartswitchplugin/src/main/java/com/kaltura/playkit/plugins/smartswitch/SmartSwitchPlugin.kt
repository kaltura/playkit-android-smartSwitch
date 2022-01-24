package com.kaltura.playkit.plugins.smartswitch

import android.content.Context
import android.webkit.URLUtil
import com.kaltura.playkit.*
import com.kaltura.playkit.plugins.smartswitch.pluginconfig.Provider
import com.kaltura.playkit.plugins.smartswitch.pluginconfig.SmartSwitchConfig
import com.kaltura.tvplayer.PKMediaEntryInterceptor
import java.util.concurrent.Future

class SmartSwitchPlugin: PKPlugin(), PKMediaEntryInterceptor {
    private val log = PKLog.get(SmartSwitchPlugin::class.java.simpleName)
    private var messageBus: MessageBus? = null

    private var accountCode: String? = null
    private var optionalParams: HashMap<String, String>? = null
    private var smartSwitchUrl: String? = null
    private var smartSwitchExecutor: SmartSwitchExecutor? = null

    override fun onLoad(player: Player?, config: Any?, messageBus: MessageBus?, context: Context?) {
        if (config == null || config !is SmartSwitchConfig) {
            log.e("SmartSwitch config is missing")
            return
        }

        this.accountCode = config.accountCode
        this.optionalParams = config.optionalParams
        this.smartSwitchUrl = config.smartSwitchUrl

        this.messageBus = messageBus
    }

    override fun apply(mediaEntry: PKMediaEntry?, listener: PKMediaEntryInterceptor.Listener?) {
        val errorCode = -1
        var errorMessage: String? = null

        if (mediaEntry == null || accountCode.isNullOrEmpty()) {
            listener?.onComplete()
            return
        }

        if (mediaEntry.sources != null && mediaEntry.sources.isNotEmpty() && mediaEntry.sources[0] != null) {
            val source = mediaEntry.sources[0]
            source?.let { mediaSource ->
                val sourceUrl = mediaSource.url
                if (!sourceUrl.isNullOrEmpty()) {
                    smartSwitchExecutor = SmartSwitchExecutor()
                    val sendRequestToYoubora: Future<Any?>? = smartSwitchExecutor?.sendRequestToYoubora(accountCode!!, sourceUrl, optionalParams, smartSwitchUrl!!)
                    val response = sendRequestToYoubora?.get()
                    if (response is String) {
                        errorMessage = response
                    } else if (response is ArrayList<*> && response.size > 0) {
                        val selectedProvider = response[0]
                        selectedProvider?.let { provider ->
                            when (provider) {
                                is Provider -> {
                                    mediaSource.url = provider.url
                                    if (URLUtil.isValidUrl(mediaSource.url)) {
                                        messageBus?.post(
                                            InterceptorEvent.CdnSwitchedEvent(
                                                InterceptorEvent.Type.CDN_SWITCHED,
                                                provider.provider))
                                    } else {
                                        errorMessage = "Invalid SmartSwitch url = ${provider.url}."
                                    }
                                }
                                else -> {
                                    errorMessage = "Unknown error in SmartSwitch response."
                                }
                            }
                        }
                    } else {
                        errorMessage = "Empty providers SmartSwitch response."
                    }
                    smartSwitchExecutor?.terminateService()
                } else {
                    errorMessage = "Invalid media source"
                }
            }
        } else {
            errorMessage = "Invalid media entry"
        }

        errorMessage?.let {
            messageBus?.post(SmartSwitchEvent.ErrorEvent(
                SmartSwitchEvent.Type.SMARTSWITCH_ERROR,
                errorCode,
                it))
        }
        listener?.onComplete()
    }

    override fun onUpdateMedia(mediaConfig: PKMediaConfig?) {

    }

    override fun onUpdateConfig(config: Any?) {

    }

    override fun onApplicationPaused() {

    }

    override fun onApplicationResumed() {

    }

    override fun onDestroy() {
        smartSwitchExecutor?.terminateService()
        messageBus?.removeListeners(this)
    }

    companion object {
        @JvmField
        val factory: Factory = object : Factory {
            override fun getName(): String {
                return "smartswitch"
            }

            override fun newInstance(): PKPlugin {
                return SmartSwitchPlugin()
            }

            override fun getVersion(): String {
                return BuildConfig.VERSION_NAME
            }

            override fun warmUp(context: Context?) {}
        }
    }
}
