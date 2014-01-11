package org.jobhearted.crawler.management;

import org.javalite.activejdbc.Model;
import org.jobhearted.crawler.database.Database;
import org.jobhearted.crawler.processing.Blacklist;
import org.jobhearted.crawler.processing.DocumentProcessor;
import org.jobhearted.crawler.processing.objects.Flag;
import org.jobhearted.crawler.processing.objects.Url;
import org.jobhearted.crawler.statistics.StatisticsTracker;
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
    //Database fields
    private static final String COL_ID = "id";
    private static final String COL_BASE_URL = "base_url";
    private static Logger logger = LoggerFactory.getLogger(CrawlManager.class);
    private static Flag[] flagPriority = {Flag.FOUND, Flag.RETRY, Flag.RECRAWL};
    //State fields
    private CrawlmanagerState state;
    //Fields used for crawling
    private UrlList urlList;
    // Fields used for processing
    private DocumentProcessor processor;
    private Blacklist blacklist;
    // Fields used for recrawl checking
    private Date lastRecrawlChecked;

    /**
     * Initializes the crawler by calling the relevant functions
     */
    public void initialize() {
        StatisticsTracker.registerCrawler(this);
        blacklist = new Blacklist(this);
        initializeList();
        checkForRecrawl();
        processor = DocumentProcessor.createProcessor(this);
    }

    /**
     * Checks if a recrawl check is needed and if so executes it.
     */
    private void checkForRecrawl() {
        long recrawlCheckTime = 10 * 60 * 1000;

        Date now = new Date();
        if (lastRecrawlChecked == null || now.getTime() - lastRecrawlChecked.getTime() > recrawlCheckTime) {
            for (Url url : urlList.getAllWithFlag(Flag.VISITED)) {
                if (url.getLastVisited() != null && now.getTime() - url.getLastVisited().getTime() > Settings.RECRAWL_TIME) {
                    url.setFlag(Flag.RECRAWL);
                }
            }
        }

        lastRecrawlChecked = now;
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
            u.setParentCrawlmanager(this);
            urlList.add(u);
            StatisticsTracker.switchFlag(this, null, u.getFlag());
        }

        if (list.isEmpty()) {
            Url url = new Url();
            url.setParentCrawlmanager(this);
            url.setFlag(Flag.FOUND);
            url.setString(Url.COL_URL, this.getString(COL_BASE_URL));
            this.addUrlToList(url);
        }
    }

    /**
     * Main method of the crawl manager. While running loops to crawl an url and send it to the processor
     */
    @Override
    public void run() {
        // open DB conn for this thread
        try {
            Database.openDatabaseConnection();

            while (this.state != CrawlmanagerState.STOPPING) {

                while (this.state == CrawlmanagerState.PAUSING || this.state == CrawlmanagerState.PAUSED) {
                    this.setState(CrawlmanagerState.PAUSED);
                    sleepForPause();
                }

                // do the crawling :)
                doTheCrawl();
            }

            this.setState(CrawlmanagerState.STOPPED);
        } catch (Exception e) {
            // Catching any exception during thread execution for both debugging as logging
            logger.warn("CrawlManager made a hard crash!", e);
        }
    }

    /**
     * Executes a crawl. Gets the url, sends it to the crawl function and sleeps for the time necessary to
     * adhere to the policy
     */
    private void doTheCrawl() {
        // create a Date for later detemination of execution length
        Date timeStarted = new Date();

        Url urlToCrawl = getUrlToCrawl();

        if (urlToCrawl != null) {
            crawlUrl(urlToCrawl);
        }

        Date timeEnded = new Date();
        sleepForPolicy(timeStarted, timeEnded);
    }

    /**
     * Checks the url list for the url of highest priority to crawl. To priority is defined in the static field
     * flagPriority, first in the array is higher.
     *
     * @return Url up for crawling next
     */
    private Url getUrlToCrawl() {
        Url found;

        for (Flag flag : flagPriority) {
            found = urlList.getFirstWithFlag(flag);
            while (found != null) {
                if (blacklist.urlAllowed(found.getUrl())) {
                    return found;
                } else {
                    urlList.remove(found);
                    found.delete();
                    found = urlList.getFirstWithFlag(flag);
                }
            }
        }
        return null;
    }

    /**
     * Actually crawls the url. Retrieves the document of the url and sends it to the processor.
     *
     * @param url Url to visit
     */
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
            logger.debug("Url connection timed out.", e);
            url.failedConnection();
        } finally {
            url.saveIt();
        }
    }

    /**
     * Function called at the end of each crawl. Sleeps for the remaining time to adhere to the timeout policy,
     * reducing stress on the webserver of the website being crawled
     *
     * @param startTime Time the crawl started
     * @param endTime   Time the crawl ended
     */
    private void sleepForPolicy(Date startTime, Date endTime) {
        long timeToSleep = Settings.CRAWL_TIMEOUT - (endTime.getTime() - startTime.getTime());

        if (timeToSleep > 0) {
            try {
                sleep(timeToSleep);
            } catch (InterruptedException e) {
                logger.debug("", e);
            }
        }
    }

    /**
     * Adds the url to the url list after saving it to the database, so all data is consistent
     *
     * @param url Url to add
     */
    public void addUrlToList(Url url) {
        url.saveIt();
        this.urlList.add(url);
    }

    /**
     * Returns the state of the crawlmanager. To represent the current state the CrawlmanagerState Enumeration is used.
     *
     * @return State of the Crawlmanager
     * @see CrawlmanagerState
     */
    public CrawlmanagerState getState() {
        return this.state;
    }

    /**
     * Sets the state of the crawlmanager. To represent the current state the CrawlmanagerState Enumeration is used.
     *
     * @param newState new state of the Crawlmanager
     * @see CrawlmanagerState
     */
    public void setState(CrawlmanagerState newState) {
        StatisticsTracker.switchCrawlerState(this, newState);
        this.state = newState;
    }

    /**
     * Returns the current UrlList. Used in the blacklist for checking if the url already exists in our data.
     *
     * @return UrlList
     */
    public UrlList getUrlList() {
        return urlList;
    }

    /**
     * Returns the current Blacklist Class, used for GUI blacklist editing
     *
     * @return Blacklist class
     */
    public Blacklist getBlacklist() {
        return this.blacklist;
    }

    /**
     * Getter for the id field of the crawlManager
     *
     * @return id value
     */
    public int getID() {
        return this.getInteger(COL_ID);
    }

    /**
     * Getter for the base_url field of the CrawlManager
     *
     * @return The base URL
     */
    public String getBaseUrl() {
        return this.getString(COL_BASE_URL);
    }

    /**
     * Overriding the toString method to provide extra information about the state. Useful when using it in a ListModel,
     * as done in the GUI.
     *
     * @return Description
     */
    @Override
    public String toString() {
        if (getState() != null) {
            return getBaseUrl() + "  -  " + getState().toString();
        } else {
            return getBaseUrl() + " - LOADING";
        }
    }

    private void sleepForPause() {
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            logger.warn("", e);
        }
    }
}
