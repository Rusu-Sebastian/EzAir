package com.proiect.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationManager {
    private static final Logger logger = Logger.getLogger(ConfigurationManager.class.getName());
    private static ConfigurationManager instance;
    private final Properties properties;

    private ConfigurationManager() {
        properties = new Properties();
        loadProperties();
    }

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getResourceAsStream("/com/proiect/config.properties")) {
            if (input == null) {
                logger.severe("Nu s-a putut găsi fișierul config.properties");
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Eroare la încărcarea config.properties", ex);
        }
    }

    // Server configuration
    public String getServerUrl() {
        return properties.getProperty("server.url", "http://localhost:3000");
    }

    public int getServerTimeout() {
        return Integer.parseInt(properties.getProperty("server.timeout", "30000"));
    }

    public int getMaxRetries() {
        return Integer.parseInt(properties.getProperty("server.retries", "3"));
    }

    public int getRetryDelay() {
        return Integer.parseInt(properties.getProperty("server.retry.delay", "1000"));
    }

    public int getConnectTimeout() {
        return Integer.parseInt(properties.getProperty("server.connect.timeout", "5000"));
    }

    public int getReadTimeout() {
        return Integer.parseInt(properties.getProperty("server.read.timeout", "30000"));
    }

    // Application configuration
    public String getAppName() {
        return properties.getProperty("app.name", "EzAir");
    }

    public String getAppVersion() {
        return properties.getProperty("app.version", "1.0.0");
    }

    public int getLoginTimeout() {
        return Integer.parseInt(properties.getProperty("app.login.timeout", "3600"));
    }

    // Error messages
    public String getServerUnavailableMessage() {
        return properties.getProperty("error.server.unavailable", "Serverul nu este disponibil");
    }

    public String getServerTimeoutMessage() {
        return properties.getProperty("error.server.timeout", "Conexiunea a expirat");
    }

    public String getNetworkErrorMessage() {
        return properties.getProperty("error.network", "Eroare de rețea");
    }

    // Helper method to reload configuration
    public void reloadConfiguration() {
        loadProperties();
    }
}
