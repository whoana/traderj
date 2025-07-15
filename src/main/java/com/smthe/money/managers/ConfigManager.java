package com.smthe.money.managers;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smthe.money.config.Configuration;

public class ConfigManager {
    
    Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigManager.class);

    private static ConfigManager configManager = null;
    
    private Configuration configuration;

    public static ConfigManager getInstance() throws StreamReadException, DatabindException, IOException {
        if (configManager == null) {
            configManager = new ConfigManager();
            configManager.load();
        }
        return configManager;
    }
    
    public void load() throws StreamReadException, DatabindException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        
        String home = System.getProperty("traderj.home", ".");
        File configFilePath = new File(home, "config"); 
        File configFile = new File(configFilePath, "bots-config.json");
        if (configFile.exists()) {
            configuration = objectMapper.readValue(configFile, Configuration.class);             
        } else {
            throw new RuntimeException("Configuration file not found: " + configFile.getAbsolutePath());
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public static void main(String[] args) {
        try {
            ConfigManager configManager = ConfigManager.getInstance();
            Configuration config = configManager.getConfiguration();
            System.out.println("Configuration loaded successfully: " + config.getDescription());
            System.out.println("Bots: " + config.getBots().size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
