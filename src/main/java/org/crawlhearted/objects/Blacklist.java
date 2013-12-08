package org.crawlhearted.objects;

import java.util.List;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/8/13
 * Time: 8:51 PM
 */
public class Blacklist {
    private List<BlacklistEntry> entries;

    public Blacklist(int crawlerId) {
        entries = BlacklistEntry.find("crawler_id = " + crawlerId);
    }
}
