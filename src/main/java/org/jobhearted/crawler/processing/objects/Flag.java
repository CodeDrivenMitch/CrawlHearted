package org.jobhearted.crawler.processing.objects;

/**
 * The Url flag enumeration for usage in the Url class and every class that works with Url. Represents the current state
 * of the url.
 */
public enum Flag {
    FOUND,
    VISITED,
    FILE,
    RETRY,
    DEAD,
    RECRAWL
}
