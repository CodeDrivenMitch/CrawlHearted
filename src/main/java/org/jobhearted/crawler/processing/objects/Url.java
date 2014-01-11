package org.jobhearted.crawler.processing.objects;

import org.javalite.activejdbc.Model;
import org.jobhearted.crawler.management.CrawlManager;
import org.jobhearted.crawler.management.Settings;
import org.jobhearted.crawler.statistics.StatisticsTracker;

import java.sql.Timestamp;
import java.util.Date;

/**
 * ActiveJDBC model for the Urls in the database.
 */
public class Url extends Model {
    // Database field names
    public static final String COL_ID = "id";
    public static final String COL_URL = "url";
    public static final String COL_FLAG = "flag";
    public static final String COL_RETRIES = "number_of_retries";
    public static final String COL_FIRST_SEEN = "first_visited";
    public static final String COL_LAST_SEEN = "last_visited";
    public static final String COL_CRAWLER_ID = "crawler_id";

    private CrawlManager crawlManager;

    // Model validors
    static {
        validatePresenceOf(COL_URL, COL_FLAG, COL_CRAWLER_ID);
    }

    /**
     * Getter for the Flag field
     *
     * @return Flag of the url
     * @see Flag
     */
    public Flag getFlag() {
        if (this.getString(COL_FLAG) != null) {
            return Flag.valueOf(this.getString(COL_FLAG));
        }
        return null;
    }

    /**
     * Sets the flag of the url, also updates the first and last visited fields if the flag indicates the visit has
     * been successful
     *
     * @param flag The new flag of the url
     */
    public void setFlag(Flag flag) {
        StatisticsTracker.switchFlag(crawlManager, getFlag(), flag);
        if (flag != null) {
            this.set(COL_FLAG, flag.toString());
            if (flag == Flag.VISITED) {
                Timestamp now = new Timestamp(new Date().getTime());
                this.setTimestamp(COL_LAST_SEEN, now);
                if (this.getTimestamp(COL_FIRST_SEEN) == null) {
                    this.setTimestamp(COL_FIRST_SEEN, now);
                }
            }
        }
    }

    /**
     * Method which is called when the Crawler couldn't visit the page. Decides whether visiting the url should
     * be tried again, and if not changed the flag to dead.
     */
    public void failedConnection() {
        if (this.getFlag() == Flag.RETRY) {
            int currentNumber = this.getInteger(COL_RETRIES);
            if (currentNumber >= Settings.RETRY_POLICY) {
                this.setFlag(Flag.DEAD);
            } else {
                this.setInteger(COL_RETRIES, currentNumber + 1);
            }
        } else {
            this.setFlag(Flag.RETRY);
            this.setInteger(COL_RETRIES, 1);
        }
    }

    /**
     * Makes it so the hashCode function only returns the hashcode of the url field, as intended
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return this.getString(COL_URL).hashCode();
    }

    /**
     * Overridden the tostring method to make sure it prints out the url
     *
     * @return
     */
    @Override
    public String toString() {
        return this.getString(COL_URL);
    }

    /**
     * Overridden the equals method to make sure it only compares the url fields.
     *
     * @param obj object to compare to
     * @return whether the object is equal or not.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(Url.class)) {
            Url other = (Url) obj;
            return other.get(COL_URL).equals(this.getString(COL_URL));
        } else {
            return false;
        }
    }

    /**
     * Sets the crawlManager the url belongs to, for saving it to the database.
     *
     * @param crawlmanager Crawlmanager it belongs to
     */
    public void setParentCrawlmanager(CrawlManager crawlmanager) {
        this.setInteger(Url.COL_CRAWLER_ID, crawlmanager.getInteger("id"));
        this.crawlManager = crawlmanager;

    }

    /**
     * Getter for the ID field in the database
     *
     * @return Id of the url
     */
    public int getID() {
        return this.getInteger(COL_ID);
    }

    /**
     * Getter for the url field in the database
     *
     * @return Url of the url
     */
    public String getUrl() {
        return this.getString(COL_URL);
    }

    /**
     * Getter for the last visited field in the database
     *
     * @return Timestamp last visited
     */
    public Timestamp getLastVisited() {
        return this.getTimestamp(COL_LAST_SEEN);
    }

}
