package org.jobhearted.crawler.management;

import org.jobhearted.crawler.database.Database;
import org.jobhearted.crawler.exceptions.UnableToStartManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * The process Manager handles all the different crawlers in our arsenal. handles things like pausing them,
 * Starting them up and other administrative tasks.
 */
public class ProcessManager {
    private static Logger logger = LoggerFactory.getLogger(ProcessManager.class);
    List<CrawlManager> crawlManagerList;
    ThreadPoolExecutor crawlExecutor;


    /**
     * Initializes the process manager, used for governing the crawlers and supplying other functions.
     * Opens the database connection, initializes the crawl managers and starts them.
     *
     * @throws SQLException Throws this exception when database connection fails
     */
    private ProcessManager() throws SQLException {
        logger.info("Initializing the ProcessManager module");
        if (!Database.loadSettings()) {
            Database.loadSettings();
        }
        Database.openDatabaseConnection();

        // initialize each crawler and then submit it to the executor
        crawlManagerList = CrawlManager.findAll();
        crawlExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(crawlManagerList.size());

        for (CrawlManager crawlManager : crawlManagerList) {
            logger.info("Initializing CrawlManager with id {}  for base url {}", crawlManager.getString("base_url"), crawlManager.getString("id"));
            crawlManager.initialize();
            crawlExecutor.submit(crawlManager);
            logger.debug("CrawlManager {} succesfully started!", crawlManager.getString("id"));
            crawlManager.setState(CrawlmanagerState.PAUSED);
        }

    }

    /**
     * Creates a Process Manager
     *
     * @return the new process manager
     * @throws org.jobhearted.crawler.exceptions.UnableToStartManagerException
     *          Error during process manager startup
     */
    public static ProcessManager createProcessManager() throws UnableToStartManagerException {
        try {
            return new ProcessManager();
        } catch (Exception e) {
            throw new UnableToStartManagerException("Could not create the process manager", e);
        }
    }
}
