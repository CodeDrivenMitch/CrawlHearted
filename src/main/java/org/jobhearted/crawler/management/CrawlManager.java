package org.jobhearted.crawler.management;

import org.jobhearted.crawler.database.Database;
import org.jobhearted.crawler.objects.Flag;
import org.jobhearted.crawler.objects.Url;
import org.jobhearted.crawler.objects.UrlList;
import org.jobhearted.crawler.processing.DocumentProcessor;
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
    private CrawlmanagerState state;
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
        }

        if(list.isEmpty()) {
            Url url = new Url();
            url.setFlag(Flag.FOUND);
            url.setString(Url.COL_URL, this.getString("base_url"));
            url.setInteger(Url.COL_CRAWLER_ID, this.getInteger("id"));
            this.addUrlToList(url);
        }
    }

    /**
     * Main method of the crawl manager. While running loops to crawl an url and send it to the processor
     */
    @Override
    public void run() {
        // open DB conn for this thread
        Database.openDatabaseConnection();

        while (this.state != CrawlmanagerState.STOPPING) {

            while (this.state == CrawlmanagerState.PAUSING || this.state == CrawlmanagerState.PAUSED) {
                this.setState(CrawlmanagerState.PAUSED);
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    logger.warn("", e);
                }
            }

            // do the crawling :)
            doTheCrawl();
        }

        this.setState(CrawlmanagerState.STOPPED);
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
        Url found;

        for(Flag f : flagPriority) {
            found = this.urlList.getFirstWithFlag(f);
            if( found.getString(Url.COL_URL) != null) {
                return found;
            }
        }

        return null;
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
        } finally {
            url.saveIt();
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
            url.saveIt();
            this.urlList.add(url);
            logger.debug("Url added: " + url.toString() + " - Valid: " + url.isValid());
        }
    }

    public CrawlmanagerState getState() {
        return this.state;
    }

    public void setState(CrawlmanagerState newState) {
        this.state = newState;
    }
}
