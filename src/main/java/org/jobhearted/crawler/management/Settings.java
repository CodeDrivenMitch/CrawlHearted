package org.jobhearted.crawler.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * class to store all application related settings. Makes use of the java Properties class to read and store them
 * to the configuration file.
 */
public class Settings {
    private static Logger LOGGER = LoggerFactory.getLogger(Settings.class);
    // Setting file constants
    private static final String SETTING_CRAWL_TIMEOUT = "crawl_timeout";
    private static final String SETTING_RETRY_POLICY = "retry_policy";
    private static final String SETTING_RECRAWL_TIME = "recrawl_time";
    private static final String SETTING_RECRAWL_CHECK_TIME = "recrawl_check_time";
    private static final String CONFIG_FILE = "jobhearted.cfg";

    // Settings
    public static int RECRAWL_TIME; // time to recrawl in miliseconds, default is 1 day
    public static int RETRY_POLICY; // Time the crawler will revisit the page before it is marker dead.
    public static int CRAWL_TIMEOUT; // Time in which maximum 1 crawl can occur. Default is 10 seconds
    public static int RECRAWL_CHECK_TIME; // How often the crawlmanager check for recrawl, default is 10 minutes

    /**
     * Reads the settings from the configuration file.
     */
    public static void loadSettings() {
        Properties configFile = new java.util.Properties();
        try {
            configFile.load(new FileInputStream(CONFIG_FILE));

            CRAWL_TIMEOUT = Integer.parseInt(configFile.getProperty(SETTING_CRAWL_TIMEOUT));
            RETRY_POLICY = Integer.parseInt(configFile.getProperty(SETTING_RETRY_POLICY));
            RECRAWL_TIME = Integer.parseInt(configFile.getProperty(SETTING_RECRAWL_TIME));
            RECRAWL_CHECK_TIME = Integer.parseInt(configFile.getProperty(SETTING_RECRAWL_CHECK_TIME));
        } catch (IOException e) {
            LOGGER.warn("Unable to open settings file, loading defaults!", e);
            createDefaultSettingsFile();
        } catch (NumberFormatException e) {
            LOGGER.warn("Was unable to read the config file, loading defaults!", e);
            createDefaultSettingsFile();
        }
    }

    /**
     * Saves the current Settings to the configuration file
     */
    public static void saveSettings() {
        Properties configFile = new java.util.Properties();

        configFile.put(SETTING_RETRY_POLICY, Integer.toString(RETRY_POLICY));
        configFile.put(SETTING_CRAWL_TIMEOUT, Integer.toString(CRAWL_TIMEOUT));
        configFile.put(SETTING_RECRAWL_TIME, Integer.toString(RECRAWL_TIME));
        configFile.put(SETTING_RECRAWL_CHECK_TIME, Integer.toString(RECRAWL_CHECK_TIME));
        try {
            configFile.store(new FileOutputStream(CONFIG_FILE), " This is the General settings file for the JobHearted Crawl application \r\n Last saved:");
        } catch (IOException e) {
            LOGGER.warn("Unable to save Settings", e);
        }
    }

    /**
     * Creates a default Settings file, used when the configuration file is not found or corrupt.
     */
    private static void createDefaultSettingsFile() {
        // Set default settings and then save it.
        CRAWL_TIMEOUT = 10000;
        RETRY_POLICY = 5;
        RECRAWL_TIME = 86400000;
        RECRAWL_CHECK_TIME = 600000;

        saveSettings();
    }
}
