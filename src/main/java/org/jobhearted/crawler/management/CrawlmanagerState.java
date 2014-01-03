package org.jobhearted.crawler.management;

/**
 * This enumeration represents the current state of a Crawlmanager. It also contains transitions like PAUSING and
 * STOPPING, because we'd like to wait for the current task to end before transitioning to the next state.
 */
public enum CrawlmanagerState {
    PAUSING,
    PAUSED,
    RUNNING,
    STOPPING,
    STOPPED
}
