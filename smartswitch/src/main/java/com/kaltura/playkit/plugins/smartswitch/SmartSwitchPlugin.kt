package com.kaltura.playkit.plugins.smartswitch

import android.content.Context
import android.text.TextUtils
import com.kaltura.playkit.*
import com.kaltura.playkit.plugins.smartswitch.pluginconfig.SmartSwitchConfig
import com.kaltura.tvplayer.PKMediaEntryInterceptor
import java.util.concurrent.Future

class SmartSwitchPlugin: PKPlugin(), PKMediaEntryInterceptor {
    private val log = PKLog.get("SmartSwitchPlugin")
    private var messageBus: MessageBus? = null

    private var accountCode: String? = null
    private var originCode: String? = null
    private var optionalParams: HashMap<String, String>? = null

    override fun onLoad(player: Player?, config: Any?, messageBus: MessageBus?, context: Context?) {
        if (config == null) {
            log.e("SmartSwitch config is missing");
            return;
        }

        if (config is SmartSwitchConfig) {
            this.accountCode = config.accountCode
            this.originCode = config.originCode
            this.optionalParams = config.optionalParams
        }

        this.messageBus = messageBus
    }

    override fun apply(mediaEntry: PKMediaEntry?, listener: PKMediaEntryInterceptor.Listener?) {
        val errorCode = -1
        var errorMessage: String? = null

        if (mediaEntry == null && TextUtils.isEmpty(accountCode) && TextUtils.isEmpty(originCode)) {
            listener?.onComplete()
            return
        }

        if (mediaEntry?.sources != null && mediaEntry.sources.isNotEmpty() && mediaEntry.sources[0] != null) {
            val source = mediaEntry.sources[0]
            source?.let { mediaSource ->
                val sourceUrl = mediaSource.url
                if (!TextUtils.isEmpty(sourceUrl)) {
                    val youboraSmartSwitchExecutor = YouboraSmartSwitchExecutor()
                    val sendRequestToYoubora: Future<Pair<String, String>?>? = youboraSmartSwitchExecutor.sendRequestToYoubora(accountCode!!, originCode!!, sourceUrl, optionalParams)
                    val responsePair = sendRequestToYoubora?.get() as Pair
                    val isErrorResponse = responsePair.second
                    val url = responsePair.first
                    if (isErrorResponse.isEmpty()) {
                        mediaSource.setUrl(url)
                    } else {
                        errorMessage = isErrorResponse
                    }
                } else {
                    errorMessage = "Invalid media source"
                }
            }
        } else {
            errorMessage = "Invalid media entry"
        }

        errorMessage?.let {
            messageBus?.post(SmartSwitchEvent.ErrorEvent(
                    SmartSwitchEvent.Type.ERROR,
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
        messageBus?.removeListeners(this)
    }

    companion object {
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