package com.kaltura.playkit.plugins.smartswitch

import android.net.Uri
import androidx.annotation.Nullable
import com.google.gson.Gson
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.plugins.smartswitch.pluginconfig.CDNList
import com.kaltura.playkit.plugins.smartswitch.pluginconfig.SmartSwitchErrorResponse
import com.kaltura.playkit.plugins.smartswitch.pluginconfig.SmartSwitchParser
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

internal class SmartSwitchExecutor {

    private val smartSwitchExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun sendRequestToYoubora(accountCode: String, originCode: String,
                             resourceUrl: String?,
                             @Nullable optionalParams: HashMap<String, String>?,
                             @Nullable smartSwitchUrl: String): Future<Pair<String, String?>?>? {
        val sendConfigToYoubora = SendConfigToYoubora(smartSwitchUrl, accountCode, originCode, resourceUrl, optionalParams)
        return smartSwitchExecutor.submit(sendConfigToYoubora)
    }

    fun terminateService() {
        smartSwitchExecutor.shutdownNow()
    }

    /**
     * Callable which is handling the Network request to CDN Balancer
     * Does the parsing of the response
     * Sends the callback as well
     */
    private class SendConfigToYoubora(val smartSwitchServerUrl: String,
                                      val accountCode: String,
                                      val originCode: String,
                                      var resourceUrl: String?,
                                      val optionalParams: HashMap<String, String>?): Callable<Pair<String, String?>?> {

        private val log: PKLog = PKLog.get("SmartSwitchExecutor")

        private val connectionReadTimeOut: Int = 10000
        private val connectionTimeOut: Int = 10000
        private val successResponseCode: Int = 200
        private val requestMethod: String = "GET"
        private val accountCodeKey = "accountCode"
        private val resourceKey = "resource"
        private val originCodeKey = "originCode"
        private var errorMessage = "Invalid Response"

        override fun call(): Pair<String, String?> {
            var connection: HttpURLConnection? = null
            var inputStream: InputStream? = null
            var bufferedReader: BufferedReader? = null
            val smartSwitchUri: Uri
            try {
                smartSwitchUri = appendQueryParams(Uri.parse(smartSwitchServerUrl))
                val url = URL(smartSwitchUri.toString())
                log.d("formatted URL: $url")
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = requestMethod
                connection.readTimeout = connectionReadTimeOut
                connection.connectTimeout = connectionTimeOut
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doInput = true
                connection.connect()

                if (connection.responseCode == successResponseCode) {
                    inputStream = connection.inputStream
                    bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var incomingData: String?
                    while (bufferedReader.readLine().also { incomingData = it } != null) {
                        stringBuilder.append(incomingData)
                    }
                    log.d("SmartSwitch Response: ${stringBuilder}")
                    val smartSwitchParser: SmartSwitchParser? = Gson().fromJson(stringBuilder.toString(), SmartSwitchParser::class.java)
                    if (smartSwitchParser?.smartSwitch != null) {
                        val cdnList: CDNList? = parseSmartSwitchResponse(smartSwitchParser)
                        if (cdnList != null) {
                            resourceUrl = cdnList.URL
                            log.d("Success response CDN_URL: ${cdnList.URL} CDN_NAME: ${cdnList.CDN_NAME}")
                            log.d("Success response CDN_CODE: ${cdnList.CDN_CODE} CDN_SCORE: ${cdnList.CDN_SCORE}")
                        } else {
                            errorMessage = "CDNList is empty"
                            return error(resourceUrl, errorMessage)
                        }
                    } else {
                        val smartSwitchError: SmartSwitchErrorResponse? = Gson().fromJson(stringBuilder.toString(), SmartSwitchErrorResponse::class.java)
                        smartSwitchError?.let {
                            it.messages?.size?.let { messageSize ->
                                if (messageSize > 0) {
                                    it.messages?.get(0)?.message?.let { message ->
                                        if (message.isNotEmpty()) {
                                            errorMessage = message
                                        }
                                    }
                                }
                            }
                        }
                        return error(resourceUrl, errorMessage)
                    }
                } else {
                    errorMessage = connection.responseMessage
                    log.d("connection.responseMessage: $errorMessage")
                    log.d("connection.responseCode: ${connection.responseCode}")
                    return error(resourceUrl, errorMessage)
                }
            } catch (malformedUrlException: MalformedURLException) {
                log.d("SmartSwitch MalformedURLException: ${malformedUrlException.message}")
                malformedUrlException.message?.let {
                    errorMessage = "SmartSwitch MalformedURLException: $it"
                }
                return error(resourceUrl, errorMessage)
            } catch (exception: IOException) {
                log.d("SmartSwitch IOException: ${exception.message}")
                exception.message?.let {
                    errorMessage = "SmartSwitch IOException: $it"
                }
                return error(resourceUrl, errorMessage)
            } finally {
                bufferedReader?.close()
                inputStream?.close()
                connection?.disconnect()
                log.d("Connection resources has been cleaned.")
            }

            return Pair(resourceUrl!!, null)
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
                        if (!queryKey.isNullOrEmpty()) {
                            builder.appendQueryParameter(queryKey, queryValue)
                        }
                    }
                }
            }

            return builder.build()
        }

        /**
         * Parse the SmartSwitch API response
         */
        private fun parseSmartSwitchResponse(smartSwitchParser: SmartSwitchParser?): CDNList? {
            var listOfCdn: CDNList? = null
            smartSwitchParser?.let { it ->
                it.smartSwitch?.let { smartSwitch ->
                    smartSwitch.CDNList?.let { cdnList ->
                        if (!cdnList.isNullOrEmpty()) {
                            cdnList[0].forEach { (_, value) ->
                                value?.let {
                                    listOfCdn = value
                                }
                            }
                        }
                    }
                }
            }
            return listOfCdn
        }

        private fun error(url: String?, errorMessage: String): Pair<String, String> {
            return Pair(url!!, errorMessage)
        }
    }
}
