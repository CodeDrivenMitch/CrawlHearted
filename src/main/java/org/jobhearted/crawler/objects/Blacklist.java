package org.jobhearted.crawler.objects;

import org.jobhearted.crawler.management.CrawlManager;

import java.util.List;

/**
 * Blacklist of words that an url can not contain. Easy to use, just initialize it with the crawler id and then call
 * urlAllowed() when you want to check if the url is allowed.
 */
public class Blacklist {
    private List<BlacklistEntry> entries;
    private String baseUrl;
    private int crawlerId;

    /**
     * Constructs the blacklist from the database
     *
     * @param crawlManager the Crawl Manager it belongs to
     */
    public Blacklist(CrawlManager crawlManager) {
        this.crawlerId = crawlManager.getInteger("id");
        this.baseUrl = crawlManager.getString("base_url");
        entries = BlacklistEntry.find("crawler_id = " + crawlManager.getString("id"));
    }

    /**
     * Checks the url against the blacklist entries to determine if the url is allowed for crawler usage
     *
     * @param url the Url to check against
     * @return boolean allowed
     */
    public boolean urlAllowed(String url) {
        if (!url.contains(baseUrl) || url.contains("#")) {
            return false;
        } else {
            for (BlacklistEntry entry : entries) {
                if (url.contains(entry.getString("word"))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Adds an entry to the blacklist.
     *
     * @param word    the word to add to the blacklist
     * @param persist Wether the entry should be saved to the database or not
     * @return the entry created by the function
     */
    public BlacklistEntry addEntry(String word, Boolean persist) {
        BlacklistEntry entryToAdd = new BlacklistEntry();
        entryToAdd.setString("word", word);
        entryToAdd.setInteger("crawler_id", this.crawlerId);

        if (persist && this.crawlerId != -1) {
            entryToAdd.save();
        }

        this.entries.add(entryToAdd);

        return entryToAdd;
    }
}
