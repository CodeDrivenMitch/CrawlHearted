package org.crawlhearted.management;

import org.crawlhearted.database.Database;
import org.crawlhearted.objects.Flag;
import org.crawlhearted.objects.Url;
import org.crawlhearted.objects.UrlList;
import org.crawlhearted.processing.DocumentProcessor;
import org.javalite.activejdbc.Model;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
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
    private static Flag[] flagPriority = {Flag.FOUND, Flag.RETRY, Flag.RECRAWL};
    // Fields used for processing
    private DocumentProcessor processor;

    /**
     * Initializes the crawler by calling the relevant functions
     */
    public void initialize() {
        initializeList();

        processor = DocumentProcessor.createProcessor(this);
    }

    /**
     * Initializes the Url List the Crawlmanager works with to do it's job.
     */
    private void initializeList() {
        // Initialize the list
        urlList = new UrlList();
        List<Url> list = Url.find("crawler_id = ?", this.getInteger("id"));

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
        // open DB conn for this thread
        Database.openDatabaseConnection();

        while (running) {
            while (paused) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    logger.warn("", e);
                }
            }

            // do the crawling :)
            doTheCrawl();
        }
    }

    private void doTheCrawl() {
        // create a Date for later detemination of execution length
        Date timeStarted = new Date();

        Url urlToCrawl = getUrlToCrawl();

        if(urlToCrawl != null) {
            crawlUrl(urlToCrawl);
        }

        Date timeEnded = new Date();
        sleepForPolicy(timeStarted, timeEnded);
    }

    private Url getUrlToCrawl() {
        Url found = null;

        for(Flag f : flagPriority) {
            found = this.urlList.getFirstWithFlag(f);
            if( found != null) {
                break;
            }
        }

        return found;
    }

    private void crawlUrl(Url url) {
        try {
            logger.info("Crawling " + url.getString("url"));
            Document document = Jsoup.connect(url.getString("url")).get();
            processor.processDocument(url, document);
            url.setFlag(Flag.VISITED);
        } catch (UnsupportedMimeTypeException e) {
            logger.debug("Url was file!", e);
            url.setFlag(Flag.FILE);
        } catch (IOException e) {
            logger.debug("Url connection timed out.");
            url.failedConnection();
        }
    }

    private void sleepForPolicy(Date startTime, Date endTime) {
        int sleepPolicy = 4000;
        long timeToSleep = sleepPolicy - (endTime.getTime() - startTime.getTime());

        logger.debug("Time taken was " + (sleepPolicy - timeToSleep) + " miliseconds!");
        if(timeToSleep > 0) {
            try {
                sleep(timeToSleep);
            } catch (InterruptedException e) {
                logger.debug("", e);
            }
        }
    }

    public void addUrlToList(Url url) {
        if(!this.urlList.contains(url)) {
            this.urlList.add(url);
            logger.debug("Url added: " + url.toString() + " - Valid: " + url.isValid());
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
