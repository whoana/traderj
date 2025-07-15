package com.smthe.money.config;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class BotConfig {
     
    String name;
    boolean enabled = true;
    String description = "RSI_MACD_BB_Strategy Bot";
    String className = "com.smthe.money.bot.FirstBot"; // Bot 클래스의 완전한 경로
    Map<String, Object> params = new HashMap<>();

    public BotConfig() {
        this.name = "RSI_MACD_BB_Strategy";
        this.params.put("rsiBarCount", 7);
        this.params.put("macdShortBarCount", 6);
        this.params.put("macdLongBarCount", 13);
        this.params.put("signalBarCount", 5);
        this.params.put("smaBarCount", 20);
        this.params.put("stdDevBarCount", 20);
        this.params.put("volumeBarCount", 20);
        this.params.put("avgVolumeBarCount", 20);
        this.params.put("bbLowerMultiplier", 1.05);
        this.params.put("bbUpperMultiplier", 0.95);
        this.params.put("avgVolumeMultiplier", 1.5);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    

}
