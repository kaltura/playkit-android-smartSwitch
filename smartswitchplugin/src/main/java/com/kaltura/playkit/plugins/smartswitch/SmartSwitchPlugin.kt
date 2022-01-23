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
    private var originCode: String? = null
    private var optionalParams: HashMap<String, String>? = null
    private var smartSwitchUrl: String? = null
    private var smartSwitchExecutor: SmartSwitchExecutor? = null

    override fun onLoad(player: Player?, config: Any?, messageBus: MessageBus?, context: Context?) {
        if (config == null || config !is SmartSwitchConfig) {
            log.e("SmartSwitch config is missing")
            return
        }

        this.accountCode = config.accountCode
        this.originCode = config.originCode
        this.optionalParams = config.optionalParams
        this.smartSwitchUrl = config.smartSwitchUrl

        this.messageBus = messageBus
    }

    override fun apply(mediaEntry: PKMediaEntry?, listener: PKMediaEntryInterceptor.Listener?) {
        val errorCode = -1
        var errorMessage: String? = null

        if (mediaEntry == null || accountCode.isNullOrEmpty() || originCode.isNullOrEmpty()) {
            listener?.onComplete()
            return
        }

        if (mediaEntry.sources != null && mediaEntry.sources.isNotEmpty() && mediaEntry.sources[0] != null) {
            val source = mediaEntry.sources[0]
            source?.let { mediaSource ->
                val sourceUrl = mediaSource.url
                if (!sourceUrl.isNullOrEmpty()) {
                    smartSwitchExecutor = SmartSwitchExecutor()
                    val sendRequestToYoubora: Future<Any?>? = smartSwitchExecutor?.sendRequestToYoubora(accountCode!!, originCode!!, sourceUrl, optionalParams, smartSwitchUrl!!)
                    val response = sendRequestToYoubora?.get()
                    if (response is String) {
                        errorMessage = response
                    } else if (response is ArrayList<*> && response.size > 0) {
                        val selectedProivder = response.get(0)
                        selectedProivder?.let { res ->
                            when (res) {
                                is Provider -> {
                                    mediaSource.url = res.url
                                    if (URLUtil.isValidUrl(mediaSource.url)) {
                                        messageBus?.post(
                                            InterceptorEvent.CdnSwitchedEvent(
                                                InterceptorEvent.Type.CDN_SWITCHED,
                                                res.provider
                                            )
                                        )
                                    } else {
                                        errorMessage = "Invalid SmartSwitch url = ${res.url}."
                                    }
                                }
                                else -> {
                                    errorMessage = "Unknown error in SmartSwitch response."
                                }
                            }
                        }}
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
