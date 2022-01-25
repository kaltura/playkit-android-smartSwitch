package com.kaltura.playkit.plugins.smartswitch

import android.net.Uri
import androidx.annotation.Nullable
import com.google.gson.Gson
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.plugins.smartswitch.pluginconfig.Provider
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

    @Nullable
    fun sendRequestToYoubora(@Nullable smartSwitchUrl: String,
                             resourceUrl: String?,
                             @Nullable optionalParams: HashMap<String, String>?): Future<Any?>? {
        val sendConfigToYoubora = SendConfigToYoubora(smartSwitchUrl, resourceUrl, optionalParams)
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
                                      var resourceUrl: String?,
                                      val optionalParams: HashMap<String, String>?): Callable<Any?> {

        private val log: PKLog = PKLog.get("SmartSwitchExecutor")

        private val connectionReadTimeOut: Int = 10000
        private val connectionTimeOut: Int = 10000
        private val successResponseCode: Int = 200
        private val methodNotFoundErrorResponseCode: Int = 400
        private val internalServerErrorResponseCode: Int = 500
        private val requestMethod: String = "GET"
        private val resourceKey = "resource"

        private var errorMessage = "Invalid Response"
        private var providers: List<Provider>? = null

        override fun call(): Any {
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
                    var responseStringBuilder: StringBuilder = getResponseStringBuilder(inputStream)
                    log.d("SmartSwitch Response: $responseStringBuilder")
                    val smartSwitchParser: SmartSwitchParser? = Gson().fromJson(responseStringBuilder.toString(), SmartSwitchParser::class.java)
                    if (smartSwitchParser?.providers != null) {
                        providers = smartSwitchParser.providers
                        var providersSize = providers?.size ?: 0
                        if (providers != null && providersSize > 0) {
                            log.d("Success response CDN_URL: ${providers?.get(0)?.url} CDN_NAME: ${providers?.get(0)?.name}")
                            log.d("Success response CDN_CODE: ${providers?.get(0)?.provider}")
                        } else {
                            errorMessage = "Error, providers list is empty"
                            return errorMessage
                        }
                    } else {
                        errorMessage = "Error, Invalid Response"
                        return errorMessage
                    }
                } else if (connection.responseCode == methodNotFoundErrorResponseCode || connection.responseCode == internalServerErrorResponseCode) {
                    inputStream = connection.errorStream
                    var responseStringBuilder: StringBuilder = getResponseStringBuilder(inputStream)
                    val smartSwitchError: SmartSwitchErrorResponse? = Gson().fromJson(responseStringBuilder.toString(), SmartSwitchErrorResponse::class.java)
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
                    return errorMessage
                } else {
                    errorMessage = connection.responseMessage
                    log.e("connection.responseMessage: $errorMessage")
                    log.e("connection.responseCode: ${connection.responseCode}")
                    return errorMessage
                }
            } catch (malformedUrlException: MalformedURLException) {
                log.e("SmartSwitch MalformedURLException: ${malformedUrlException.message}")
                malformedUrlException.message?.let {
                    errorMessage = "SmartSwitch MalformedURLException: $it"
                }
                return errorMessage
            } catch (exception: IOException) {
                log.e("SmartSwitch IOException: ${exception.message}")
                exception.message?.let {
                    errorMessage = "SmartSwitch IOException: $it"
                }
                return errorMessage
            } finally {
                bufferedReader?.close()
                inputStream?.close()
                connection?.disconnect()
                log.d("Connection resources have been cleaned.")
            }
            if (providers == null){
                providers = ArrayList()
            }
            return providers!!
        }

        /**
         * Parse the input stream and build a response String
         */
        private fun getResponseStringBuilder(inputStream: InputStream?): StringBuilder {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val responseStringBuilder = StringBuilder()
            var incomingData: String?
            while (bufferedReader.readLine().also { incomingData = it } != null) {
                responseStringBuilder.append(incomingData)
            }
            return responseStringBuilder
        }

        /**
         * Add the Incoming query params.
         */
        private fun appendQueryParams(uri: Uri): Uri {
            val builder: Uri.Builder = uri.buildUpon()
            builder.appendQueryParameter(resourceKey, resourceUrl)
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
    }
}
