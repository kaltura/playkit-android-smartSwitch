package com.kaltura.playkit.plugins.smartswitch

import com.kaltura.playkit.PKEvent

open class SmartSwitchEvent(type: Type?): PKEvent {

    var type: Type? = null
    var error = ErrorEvent::class.java

    init {
        this.type = type
    }

    enum class Type {
        ERROR
    }

    class ErrorEvent(errorType: Type?, val errorCode: Int, val errorMessage: String) : SmartSwitchEvent(errorType)

    override fun eventType(): Enum<*>? {
        return type
    }
}