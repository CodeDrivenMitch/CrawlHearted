package org.jobhearted.crawler;

import org.jobhearted.crawler.exceptions.UnableToStartManagerException;
import org.jobhearted.crawler.gui.MainWindow;
import org.jobhearted.crawler.management.ProcessManager;
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

    public static void main(String[] args) {
        logger.info("Starting up the CrawlHearted application");

        try {
            ProcessManager processManager = ProcessManager.createProcessManager();

            // launching org.jobhearted.crawler.gui if necessary
            List<String> arguments = Arrays.asList(args);
            if(!arguments.contains("-nogui")) {
                launchGui();
            }
        } catch (UnableToStartManagerException e) {
            logger.warn("failed to initialize!", e);
        }
    }

    private static void launchGui() {
        MainWindow.createMainWindow();
    }
}
