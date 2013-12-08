package org.crawlhearted.objects;

import java.util.ArrayList;

/**
 * List of Urls which has some functions that are useful for the crawler.
 */
public class UrlList extends ArrayList<Url> {

    /**
     * Gets the first url in the list with the parameter flag given
     *
     * @param flag Flag of the url you want
     * @return The url if there is one found. If not, returns null
     */
    public Url getFirstWithFlag(Flag flag) {
        for (Url url : this) {
            if (url.getFlag() == flag) {
                return url;
            }
        }
        return null;
    }
}
