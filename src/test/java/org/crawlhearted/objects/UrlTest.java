package org.crawlhearted.objects;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test for the Url class
 */
public class UrlTest {

    @Test
    public void testFlag() {
        Url url = new Url();

        url.setFlag(Flag.FOUND);

        assertTrue(url.getFlag() == Flag.FOUND);

        // Also test is last_visited and first_visited is set, as required
        assertTrue(url.get(Url.COL_FIRST_SEEN) != null);
        assertTrue(url.get(Url.COL_LAST_SEEN) != null);
    }

    @Test
    public void retryTest() {
        Url url = new Url();

        url.setFlag(Flag.FOUND);
        url.failedConnection();

        assertTrue(url.getFlag() == Flag.RETRY);
        assertTrue(url.getInteger(Url.COL_RETRIES) == 1);

        url.setInteger(Url.COL_RETRIES, 10000);
        url.failedConnection();

        assertTrue(url.getFlag() == Flag.DEAD);
    }
}
