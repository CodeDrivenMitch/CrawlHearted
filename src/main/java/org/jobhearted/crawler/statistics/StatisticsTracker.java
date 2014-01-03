package org.jobhearted.crawler.statistics;

import org.jobhearted.crawler.management.CrawlManager;
import org.jobhearted.crawler.management.CrawlmanagerState;
import org.jobhearted.crawler.processing.objects.Flag;
import org.jobhearted.crawler.statistics.observers.StatisticObserver;

import java.util.*;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/12/13
 * Time: 9:49 PM
 */
public class StatisticsTracker {
    private static Map<CrawlManager, Map<Flag, Integer>> flagMap = new HashMap<CrawlManager, Map<Flag, Integer>>();
    private static Map<CrawlManager, CrawlmanagerState> stateMap = new HashMap<CrawlManager, CrawlmanagerState>();
    private static List<StatisticObserver> observersToNotify = new LinkedList<StatisticObserver>();
    private static List<StatisticObserver> observers = new LinkedList<StatisticObserver>();

    public static void registerCrawler(CrawlManager crawlManager) {
        Map<Flag, Integer> crawlerMap = new LinkedHashMap<Flag, Integer>();

        for(Flag f : Flag.values()) {
            crawlerMap.put(f, 0);
        }

        flagMap.put(crawlManager, crawlerMap);
    }


    public static void removeCrawlManager(CrawlManager manager) {
        flagMap.remove(manager);
        stateMap.remove(manager);

        for(StatisticObserver o : observers) {
            o.crawlerRemoved(manager);
        }
    }

    public static void removeObserver(StatisticObserver statisticObserver) {
        if(observers.contains(statisticObserver)) {
            observers.remove(statisticObserver);
        }
        if(observersToNotify.contains(statisticObserver)) {
            observersToNotify.remove(statisticObserver);
        }
    }

    public static void switchCrawlerState(CrawlManager crawlManager, CrawlmanagerState crawlmanagerState) {
        stateMap.put(crawlManager, crawlmanagerState);
        notifyNewCrawlerstate(crawlManager, crawlmanagerState);
    }

    private static void notifyNewFlagCount(CrawlManager crawlManager, Flag flag, int count) {
        checkForObserversToNotify();
        for(StatisticObserver statisticObserver : observers) {
            statisticObserver.updateFlag(crawlManager, flag, count);
        }
    }

    private static void notifyNewCrawlerstate(CrawlManager crawlManager, CrawlmanagerState crawlmanagerState) {
        checkForObserversToNotify();
        for(StatisticObserver statisticObserver : observers) {
            statisticObserver.updateCrawlerState(crawlManager, crawlmanagerState);
        }
    }

    private static void checkForObserversToNotify() {
        if(!observersToNotify.isEmpty()) {
            for(StatisticObserver o : observersToNotify) {
                notifyWithAllData(o);
            }
        }
    }

    public static void switchFlag(CrawlManager crawlmanager, Flag oldFlag, Flag newFlag) {
        Map<Flag, Integer> specificMap = flagMap.get(crawlmanager);
        if(oldFlag != null) {
            specificMap.put(oldFlag, specificMap.get(oldFlag) - 1);
            notifyNewFlagCount(crawlmanager, oldFlag, specificMap.get(oldFlag) );
        }
        if(newFlag != null) {
            specificMap.put(newFlag, specificMap.get(newFlag) + 1);
            notifyNewFlagCount(crawlmanager, newFlag, specificMap.get(newFlag) );
        }
    }

    public static void registerObserver(StatisticObserver observer) {
        if(!observers.contains(observer)) {
            observers.add(observer);
            observersToNotify.add(observer);
        }
    }

    private static void notifyWithAllData(StatisticObserver observer) {
        for(Map.Entry<CrawlManager, Map<Flag, Integer>> entry : flagMap.entrySet()) {
            for(Map.Entry<Flag, Integer> flagEntry : entry.getValue().entrySet()) {
                observer.updateFlag(entry.getKey(), flagEntry.getKey(), flagEntry.getValue());
            }
        }

        for(Map.Entry<CrawlManager, CrawlmanagerState> entry : stateMap.entrySet()) {
            observer.updateCrawlerState(entry.getKey(), entry.getValue());
        }
    }

    private StatisticsTracker() {
        throw new UnsupportedOperationException("Can not initialize the StatisticsTracker class!");
    }
}
