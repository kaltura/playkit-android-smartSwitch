package com.kaltura.playkit.plugins.smartswitchplugin.pluginconfig;

import androidx.annotation.Nullable;

import java.util.HashMap;

public class SmartSwitchConfig {

    private String accountCode;
    private String originCode;
    private HashMap<String, String> optionalParams;

    public SmartSwitchConfig() {}

    public SmartSwitchConfig(String accountCode, String originCode, @Nullable HashMap<String, String> optionalParams) {
        this.accountCode = accountCode;
        this.originCode = originCode;
        this.optionalParams = optionalParams;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public String getOriginCode() {
        return originCode;
    }

    @Nullable
    public HashMap<String, String> getOptionalParams() {
        return optionalParams;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public void setOriginCode(String originCode) {
        this.originCode = originCode;
    }

    public void setOptionalParams(@Nullable HashMap<String, String> optionalParams) {
        this.optionalParams = optionalParams;
    }
}
