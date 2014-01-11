package org.jobhearted.crawler.statistics;

import org.jobhearted.crawler.management.CrawlManager;
import org.jobhearted.crawler.management.CrawlmanagerState;
import org.jobhearted.crawler.processing.objects.Flag;
import org.jobhearted.crawler.statistics.observers.StatisticObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * StatisticsTracker class. This is an Observable object at which StatisticObservers can register, to opt in for
 * information regarding crawlers and urls.
 * Crawlmanagers can also register to put data into the class, making it accessible to the Observers, like the GUI.
 */
public class StatisticsTracker {
    private static Logger logger = LoggerFactory.getLogger(CrawlManager.class);
    private static Map<CrawlManager, Map<Flag, Integer>> flagMap = new HashMap<CrawlManager, Map<Flag, Integer>>();
    private static Map<CrawlManager, CrawlmanagerState> stateMap = new HashMap<CrawlManager, CrawlmanagerState>();
    private static List<StatisticObserver> observersToNotify = new LinkedList<StatisticObserver>();
    private static List<StatisticObserver> observers = new LinkedList<StatisticObserver>();

    /**
     * Private constructor for this class. Since it's a static utility class, we don't want it to be initializable.
     */
    private StatisticsTracker() {
        throw new UnsupportedOperationException("Can not initialize the StatisticsTracker class!");
    }

    /**
     * Registers the crawler to the StatisticsTracker, so observers can use the data available.
     *
     * @param crawlManager CrawlManager to register
     */
    public static void registerCrawler(CrawlManager crawlManager) {
        Map<Flag, Integer> crawlerMap = new LinkedHashMap<Flag, Integer>();

        for (Flag f : Flag.values()) {
            crawlerMap.put(f, 0);
        }

        flagMap.put(crawlManager, crawlerMap);
    }

    /**
     * Removes the CrawlManager from the StatisticsTracker, so observers may remove it from their data.
     *
     * @param manager CrawlManager to unregister
     */
    public static void removeCrawlManager(CrawlManager manager) {
        flagMap.remove(manager);
        stateMap.remove(manager);

        for (StatisticObserver o : observers) {
            o.crawlerRemoved(manager);
        }
    }

    /**
     * Removes an Observer from the notifylist. For example, this method can be called when a GUI window is closing.
     *
     * @param statisticObserver StatisticObserver to remove.
     */
    public static void removeObserver(StatisticObserver statisticObserver) {
        if (observers.contains(statisticObserver)) {
            observers.remove(statisticObserver);
        }
        if (observersToNotify.contains(statisticObserver)) {
            observersToNotify.remove(statisticObserver);
        }
    }

    /**
     * Updates the state of a crawler to the statisticsTracker. When the data of the StatisticsTracker is updated,
     * it will send a notification of the even to the Observers registered with the class.
     *
     * @param crawlManager      CrawlManager to update the state of.
     * @param crawlmanagerState New state of said CrawlManager
     */
    public static void switchCrawlerState(CrawlManager crawlManager, CrawlmanagerState crawlmanagerState) {
        stateMap.put(crawlManager, crawlmanagerState);
        notifyNewCrawlerstate(crawlManager, crawlmanagerState);
    }

    /**
     * Notifies all Observers with the new count of an url flag. This is called when a flag field is updated.
     *
     * @param crawlManager Crawlmanager in which the an url flag changed.
     * @param flag         flag to updated.
     * @param count        Updated count of the url flag.
     */
    private static void notifyNewFlagCount(CrawlManager crawlManager, Flag flag, int count) {
        checkForObserversToNotify();
        for (StatisticObserver statisticObserver : observers) {
            statisticObserver.updateFlag(crawlManager, flag, count);
        }
    }

    /**
     * Notifies all observers with the new state of a CrawlManager
     *
     * @param crawlManager      Crawlmanager which' state has changed.
     * @param crawlmanagerState New state of the CrawlManager
     */
    private static void notifyNewCrawlerstate(CrawlManager crawlManager, CrawlmanagerState crawlmanagerState) {
        checkForObserversToNotify();
        for (StatisticObserver statisticObserver : observers) {
            statisticObserver.updateCrawlerState(crawlManager, crawlmanagerState);
        }
    }

    /**
     * Checks if there are observers who haven't received any data yet. Data is not sent on register because the
     * registering most of the times takes place in the constructor of a class, resulting in a NullpointerException.
     */
    private static void checkForObserversToNotify() {
        if (!observersToNotify.isEmpty()) {
            for (StatisticObserver o : observersToNotify) {
                notifyWithAllData(o);
            }
        }
    }

    /**
     * Called when an url switched flags. Does everything necessary to get the observers updated with that latest info.
     *
     * @param crawlmanager Crawlmanager the Url belongs to.
     * @param oldFlag      Old flag of the url
     * @param newFlag      New flag of the url
     */
    public static void switchFlag(CrawlManager crawlmanager, Flag oldFlag, Flag newFlag) {
        Map<Flag, Integer> specificMap = flagMap.get(crawlmanager);
        if (oldFlag != null) {
            specificMap.put(oldFlag, specificMap.get(oldFlag) - 1);
            notifyNewFlagCount(crawlmanager, oldFlag, specificMap.get(oldFlag));
        }
        if (newFlag != null) {
            specificMap.put(newFlag, specificMap.get(newFlag) + 1);
            notifyNewFlagCount(crawlmanager, newFlag, specificMap.get(newFlag));
        }
    }

    /**
     * Registers a StatisticsObserver with the StatisticsTracker, making it eligible to receive information when any
     * is updated. The Observer is added to a list to receive the data later.
     *
     * @param observer Observer to add.
     */
    public static void registerObserver(StatisticObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            observersToNotify.add(observer);
        }
    }

    /**
     * Notifies an observer with all data we have, so it has all the data it could need.
     *
     * @param observer Observer to send data to.
     */
    private static void notifyWithAllData(StatisticObserver observer) {
        try {
            for (Map.Entry<CrawlManager, Map<Flag, Integer>> entry : flagMap.entrySet()) {
                for (Map.Entry<Flag, Integer> flagEntry : entry.getValue().entrySet()) {
                    observer.updateFlag(entry.getKey(), flagEntry.getKey(), flagEntry.getValue());
                }
            }
            for (Map.Entry<CrawlManager, CrawlmanagerState> entry : stateMap.entrySet()) {
                observer.updateCrawlerState(entry.getKey(), entry.getValue());
            }

            observersToNotify.remove(observer);
        } catch (NullPointerException e) {
            logger.info("Could not update the observer yet!", e);
        }


    }
}
