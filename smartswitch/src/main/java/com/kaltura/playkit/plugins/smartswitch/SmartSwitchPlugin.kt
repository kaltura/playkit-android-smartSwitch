package com.kaltura.playkit.plugins.smartswitch

import android.text.TextUtils
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.tvplayer.PKMediaEntryInterceptor

class SmartSwitchPlugin: PKMediaEntryInterceptor {
    private val log = PKLog.get("SmartSwitchPlugin")

    override fun apply(mediaEntry: PKMediaEntry?, listener: PKMediaEntryInterceptor.Listener?) {
        log.e("in interceptor Apply  =>  " + mediaEntry!!.id)
        //TODO
        /*if (TextUtils.isEmpty(accountCode) && TextUtils.isEmpty(origincode)) {
            listener!!.onComplete()
            return
        }

        if (mediaEntry != null && mediaEntry.sources != null &&
                !mediaEntry.sources.isEmpty() && mediaEntry.sources[0] != null) {
            val sourceUrl = mediaEntry.sources[0].url
            if (!TextUtils.isEmpty(sourceUrl)) {
                val youboraSmartSwitchExecutor = YouboraSmartSwitchExecutor()
                youboraSmartSwitchExecutor.sendRequestToYoubora(accountCode, origincode, sourceUrl, optionalParams)
            }
        }*/
        listener!!.onComplete()
    }
}