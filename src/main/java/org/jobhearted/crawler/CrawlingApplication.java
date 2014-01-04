package org.jobhearted.crawler;

import org.apache.log4j.PropertyConfigurator;
import org.jobhearted.crawler.exceptions.UnableToStartManagerException;
import org.jobhearted.crawler.gui.MainWindow;
import org.jobhearted.crawler.management.ProcessManager;
import org.jobhearted.crawler.management.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Main Class of the JobHearted Crawl Application
 * Launches the application and open the GUI if needed
 */
public class CrawlingApplication {
    private static Logger logger = LoggerFactory.getLogger(CrawlingApplication.class);

    /**
     * Main method for the application. Initializes logging, loads settings, launches the GUI if needed and creates
     * the process manager
     *
     * @param args
     */
    public static void main(String[] args) {
        PropertyConfigurator.configure("logging.cfg");
        logger.info("Starting up the CrawlHearted application");
        Settings.loadSettings();

        try {


            // launching org.jobhearted.crawler.gui if necessary
            List<String> arguments = Arrays.asList(args);
            if (!arguments.contains("-nogui")) {
                launchGui();
            }

            ProcessManager.createProcessManager();
        } catch (UnableToStartManagerException e) {
            logger.warn("failed to initialize!", e);
        }
    }

    /**
     * Launches the window if there was no -nogui parameter when launching the program.
     */
    private static void launchGui() {
        MainWindow.createMainWindow();
    }

    /**
     * Private constructor to hide the public implicit one
     */
    private CrawlingApplication() {
    }
}
