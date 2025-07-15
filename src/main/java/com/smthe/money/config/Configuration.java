package com.smthe.money.config;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Configuration {
    
    String description = "TraderJ Configuration";
    
    String version = "1.0.0";

    String lastUpdated = "2023-10-01";

    String author = "TraderJ Team";
    
    List<BotConfig> bots = new ArrayList<>();


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<BotConfig> getBots() {
        return bots;
    }

    public void setBots(List<BotConfig> bots) {
        this.bots = bots;
    }

    
}
