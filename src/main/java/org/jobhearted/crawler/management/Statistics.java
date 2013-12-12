package org.jobhearted.crawler.management;

import org.jobhearted.crawler.objects.Flag;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/8/13
 * Time: 7:02 PM
 */
public class Statistics {
    private static Statistics ourInstance = new Statistics();

    /**
     * Returns the unique Statistics instance
     * @return Statistics
     */
    public static Statistics getInstance() {
        return ourInstance;
    }

    /**
     * Initializes the Statistics class
     */
    private Statistics() {
    }

    public void urlFlagChanged(int crawlerId, Flag oldFlag, Flag newFlag) {

    }
}
