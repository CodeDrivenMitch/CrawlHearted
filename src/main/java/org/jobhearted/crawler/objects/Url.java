package org.jobhearted.crawler.objects;

import org.javalite.activejdbc.Model;
import org.jobhearted.crawler.management.CrawlManager;
import org.jobhearted.crawler.statistics.StatisticsTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/8/13
 * Time: 7:53 PM
 */
public class Url extends Model {
    private static Logger logger = LoggerFactory.getLogger(Url.class);
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
     * @return Flag of the url
     * @see Flag
     */
    public Flag getFlag() {
        if(this.getString(COL_FLAG) != null) {
            return Flag.valueOf(this.getString(COL_FLAG));
        }
        return null;
    }

    /**
     *  Sets the flag of the url, also updates the first and last visited fields if the flag indicates the visit has
     *  been successful
     * @param flag The new flag of the url
     */
    public void setFlag(Flag flag) {
        StatisticsTracker.switchFlag(crawlManager, getFlag(), flag);
        if(flag != null) {
            this.set(COL_FLAG, flag.toString());
            if(flag == Flag.VISITED) {
                Timestamp now = new Timestamp(new Date().getTime());
                this.setTimestamp(COL_LAST_SEEN, now);
                if(this.getTimestamp(COL_FIRST_SEEN) == null) {
                    this.setTimestamp(COL_FIRST_SEEN, now);
                }
            }
        }
    }

    public void failedConnection() {
        if(this.getFlag() == Flag.RETRY) {
            int currentNumber = this.getInteger(COL_RETRIES);
            if(currentNumber + 1 >= 4) {
                this.setFlag(Flag.DEAD);
            } else {
                this.setInteger(COL_RETRIES, currentNumber + 1);
            }
        } else {
            this.setFlag(Flag.RETRY);
            this.setInteger(COL_RETRIES, 1);
        }
    }

    @Override
    public int hashCode() {
        return this.getString(COL_URL).hashCode();
    }

    @Override
    public String toString() {
        return this.getString(COL_URL);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass().equals(Url.class)) {
            Url other = (Url) obj;
            return other.get(COL_URL).equals(this.getString(COL_URL));
        } else {
            return false;
        }
    }

    public void setParentCrawlmanager(CrawlManager crawlmanager) {
        this.setInteger(Url.COL_CRAWLER_ID, crawlmanager.getInteger("id"));
        this.crawlManager = crawlmanager;

    }
}
