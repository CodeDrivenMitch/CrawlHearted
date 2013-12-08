package org.crawlhearted.management;

import org.crawlhearted.objects.Url;
import org.crawlhearted.objects.UrlList;
import org.javalite.activejdbc.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Crawl Manager handles crawling of a certain domain. Uses the blacklist class to check the urls for validness.
 */
public class CrawlManager extends Model implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(CrawlManager.class);
    //State fields
    private boolean running = true;
    private boolean paused = true;
    //Fields used for crawling
    private UrlList urlList;

    /**
     * Initializes the crawler by calling the relevant functions
     */
    public void initialize() {
        initializeList();
    }

    /**
     * Initializes the Url List the Crawlmanager works with to do it's job.
     */
    private void initializeList() {
        // Initialize the list
        urlList = new UrlList();
        List<Url> list = Url.find("crawler_id = " + this.getInteger("id"));

        // Register the urls at the statistics
        for (Url u : list) {
            urlList.add(u);
            Statistics.getInstance().urlFlagChanged(getInteger("id"), null, u.getFlag());
        }
    }

    /**
     * Main method of the crawl manager. While running loops to crawl an url and send it to the processor
     */
    @Override
    public void run() {
        while (running) {
            while (paused) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    logger.warn("", e);
                }
            }
        }
    }

    /**
     * Checks if the thread is running
     *
     * @return running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Sets the running field to the specified value. If false, the runnable will exit its while loop and stop execution
     *
     * @param running value running should be set to
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Checks if the Runnable is paused
     *
     * @return paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Sets the paused field to the specified value. while paused is true, thread will sleep.
     *
     * @param paused paused
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
