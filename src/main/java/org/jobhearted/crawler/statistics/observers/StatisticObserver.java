package org.jobhearted.crawler.statistics.observers;

import org.jobhearted.crawler.management.CrawlManager;
import org.jobhearted.crawler.management.CrawlmanagerState;
import org.jobhearted.crawler.processing.objects.Flag;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/12/13
 * Time: 9:29 PM
 */
public interface StatisticObserver {
    void updateFlag(CrawlManager crawlManager, Flag flag, int newCount);
    void updateCrawlerState(CrawlManager crawlManager, CrawlmanagerState newState);
    void crawlerRemoved(CrawlManager crawlManager);
}
