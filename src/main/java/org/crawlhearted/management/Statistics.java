package org.crawlhearted.management;

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
}
