package com.kaltura.playkit.plugins.smartswitchplugin

import android.net.Uri
import androidx.annotation.Nullable
import com.google.gson.Gson
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.plugins.smartswitchplugin.pluginconfig.SmartSwitchErrorResponse
import com.kaltura.playkit.plugins.smartswitchplugin.pluginconfig.SmartSwitchParser
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

internal class YouboraSmartSwitchExecutor {

    private val smartSwitchUrl = "http://cdnbalancer.youbora.com/orderedcdn"
    private val smartSwitchExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun sendRequestToYoubora(accountCode: String, originCode: String, resourceUrl: String?, @Nullable optionalParams: HashMap<String, String>?): Future<Pair<String, String>?>? {
        val sendConfigToYoubora = SendConfigToYoubora(smartSwitchUrl, accountCode, originCode, resourceUrl, optionalParams)
        return smartSwitchExecutor.submit(sendConfigToYoubora)
    }

    private class SendConfigToYoubora(val smartSwitchUrl: String,
                                      val accountCode: String,
                                      val originCode: String,
                                      var resourceUrl: String?,
                                      val optionalParams: HashMap<String, String>?): Callable<Pair<String, String>?> {

        private val log: PKLog = PKLog.get("YouboraSmartSwitchExecutor")

        private val connectionReadTimeOut: Int = 120000
        private val connectionTimeOut: Int = 120000
        private val successResponseCode: Int = 200
        private val requestMethod: String = "GET"
        private val accountCodeKey = "accountCode"
        private val resourceKey = "resource"
        private val originCodeKey = "originCode"

        override fun call(): Pair<String, String> {
            var connection: HttpURLConnection? = null
            val inputStream: InputStream?
            var smartSwitchUri: Uri
            try {
                smartSwitchUri  = Uri.parse(smartSwitchUrl)
                smartSwitchUri = appendQueryParams(smartSwitchUri)
                val url = URL(smartSwitchUri.toString())
                log.d("formatted URL: ${url}")
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = requestMethod
                connection.readTimeout = connectionReadTimeOut
                connection.connectTimeout = connectionTimeOut
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doInput = true
                connection.connect()

                if (connection.responseCode == successResponseCode) {
                    inputStream = connection.inputStream
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var incomingData: String?
                    while (bufferedReader.readLine().also { incomingData = it } != null) {
                        stringBuilder.append(incomingData)
                    }
                    bufferedReader.close()
                    inputStream.close()
                    log.d("SmartSwitch Response: ${stringBuilder}")
                    val smartSwitchParser: SmartSwitchParser? = Gson().fromJson(stringBuilder.toString(), SmartSwitchParser::class.java)
                    if (smartSwitchParser?.smartSwitch != null) {
                        resourceUrl = parseSmartSwitchResponse(smartSwitchParser, resourceUrl)
                    } else {
                        var message = "Invalid Response"
                        val smartSwitchError: SmartSwitchErrorResponse? = Gson().fromJson(stringBuilder.toString(), SmartSwitchErrorResponse::class.java)
                        smartSwitchError?.let {
                            it.messages?.get(0)?.message?.let { errorMessage ->
                                message = errorMessage
                            }
                        }
                        return Pair(resourceUrl!!, message)
                    }
                }
            } catch (malformedUrlException: MalformedURLException) {
                log.d("SmartSwitch MalformedURLException: ${malformedUrlException.message}")
            } catch (exception: IOException) {
                log.d("SmartSwitch IOException: ${exception.message}")
            } finally {
                connection?.disconnect()
            }

            return Pair(resourceUrl!!, "")
        }

        /**
         * Add the Incoming query params.
         */
        private fun appendQueryParams(uri: Uri): Uri {
            val builder: Uri.Builder = uri.buildUpon()
            builder.appendQueryParameter(accountCodeKey, accountCode)
            builder.appendQueryParameter(resourceKey, resourceUrl)
            builder.appendQueryParameter(originCodeKey, originCode)

            optionalParams?.let { it ->
                if (it.isNotEmpty()) {
                    it.forEach { (queryKey, queryValue) ->
                        builder.appendQueryParameter(queryKey, queryValue)
                    }
                }
            }

            return builder.build()
        }

        /**
         * Parse the SmartSwitch API response
         */
        private fun parseSmartSwitchResponse(smartSwitchParser: SmartSwitchParser?, resourceUrl: String?): String? {
            var url = resourceUrl
            smartSwitchParser?.let { it ->
                it.smartSwitch?.let { smartSwitch ->
                    smartSwitch.CDNList?.let { cdnList ->
                        if (cdnList.isNotEmpty()) {
                            cdnList[0].forEach { (_, value) ->
                                value?.URL.also {
                                    url = it
                                    return@forEach
                                }
                            }
                        }
                    }
                }
            }
            return url
        }
    }
}
