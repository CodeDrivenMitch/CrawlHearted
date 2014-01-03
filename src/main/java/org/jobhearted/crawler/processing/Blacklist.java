package org.jobhearted.crawler.processing;

import org.jobhearted.crawler.management.CrawlManager;
import org.jobhearted.crawler.processing.objects.BlacklistEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Blacklist of words that an url can not contain. Easy to use, just initialize it with the crawler id and then call
 * urlAllowed() when you want to check if the url is allowed.
 */
public class Blacklist {
    private static Logger logger = LoggerFactory.getLogger(Blacklist.class);
    private List<BlacklistEntry> entries;
    private String baseUrl;
    private int crawlerId;

    /**
     * Constructs the blacklist from the database
     *
     * @param crawlManager the Crawl Manager it belongs to
     */
    public Blacklist(CrawlManager crawlManager) {
        this.crawlerId = crawlManager.getID();
        this.baseUrl = crawlManager.getBaseUrl();
        this.entries = BlacklistEntry.loadAllEntriesForCrawlerId(crawlerId);
        logger.info("Loaded {} Blacklist entries for crawler {}", entries.size(), crawlManager.getId());
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
     * Adds an entry to the blacklist. This is an API part for usage in the GUI
     *
     * @param word    the word to add to the blacklist
     * @param persist Whether the entry should be saved to the database or not
     * @return the entry created by the function
     */
    public BlacklistEntry addEntry(String word, Boolean persist) {
        BlacklistEntry entryToAdd = new BlacklistEntry();
        entryToAdd.setWord(word);
        entryToAdd.setCrawlerId(crawlerId);

        if (persist) {
            entryToAdd.save();
        }

        this.entries.add(entryToAdd);

        return entryToAdd;
    }
}
