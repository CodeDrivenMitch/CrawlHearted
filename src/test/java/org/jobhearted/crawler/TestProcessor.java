package org.jobhearted.crawler;

import org.jobhearted.crawler.management.CrawlManager;
import org.jobhearted.crawler.processing.objects.Url;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 1/21/14
 * Time: 10:20 PM
 */

public class TestProcessor {
    CrawlManager crawlManager;
    String baseUrl;

    @Before
    public void prepare() {
        crawlManager = CrawlManager.findFirst("");
        crawlManager.initialize();
    }

    @Test
    public void testBlacklist() {
        baseUrl = crawlManager.getBaseUrl();
        assertFalse(crawlManager.getBlacklist().urlAllowed(baseUrl + "#"));
        assertFalse(crawlManager.getBlacklist().urlAllowed("nobodyreadsthis"));
        assertTrue(crawlManager.getBlacklist().urlAllowed(baseUrl + "/thisshouldbegood/"));
    }

    @Test
    public void testUrlValidators() {
        Url u = new Url();
        u.setString(Url.COL_URL, "http://www.google.nl/");
        u.setString(Url.COL_FLAG, "DEAD");
        assertFalse(u.isValid());
        u.setInteger(Url.COL_CRAWLER_ID, 1);
        assertTrue(u.isValid());
    }
}
