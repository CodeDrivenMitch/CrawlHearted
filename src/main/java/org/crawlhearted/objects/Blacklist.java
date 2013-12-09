package org.crawlhearted.objects;

import java.util.List;

/**
 * Blacklist of words that an url can not contain. Easy to use, just initialize it with the crawler id and then call
 * urlAllowed() when you want to check if the url is allowed.
 */
public class Blacklist {
    private List<BlacklistEntry> entries;

    /**
     * Constructs the blacklist from the database
     * @param crawlerId the Id of the crawler
     */
    public Blacklist(int crawlerId) {
        entries = BlacklistEntry.find("crawler_id = " + crawlerId);

    }

    /**
     * Checks the url against the blacklist entries to determine if the url is allowed for crawler usage
     * @param url the Url to check against
     * @return boolean allowed
     */
    public boolean urlAllowed(String url) {
        boolean allowed = true;

        for(BlacklistEntry entry : entries) {
            if( url.contains(entry.getString("word"))) {
                allowed = false;
            }
        }

        return allowed;
    }
}
