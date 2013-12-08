package org.crawlhearted.management;

import org.javalite.activejdbc.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.sleep;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/8/13
 * Time: 5:10 PM
 */
public class CrawlManager extends Model implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(CrawlManager.class);
    private boolean running = true;
    private boolean paused = true;

    public void initialize() {

    }

    /**
     * Main method of the crawl manager. While running loops to crawl an url and send it to the processor
     */
    @Override
    public void run() {
        while(running) {
            while(paused) {
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
     * @return running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Sets the running field to the specified value. If false, the runnable will exit its while loop and stop execution
     * @param running value running should be set to
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Checks if the Runnable is paused
     * @return paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Sets the paused field to the specified value. while paused is true, thread will sleep.
     * @param paused paused
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
