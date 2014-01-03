package org.jobhearted.crawler.gui;

import org.jobhearted.crawler.management.CrawlManager;
import org.jobhearted.crawler.management.CrawlmanagerState;
import org.jobhearted.crawler.processing.objects.Flag;
import org.jobhearted.crawler.statistics.observers.StatisticObserver;
import org.jobhearted.crawler.statistics.StatisticsTracker;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * The Bound class for the Main window GUI, containing general information concerning all crawlmanagers and urls, such
 * as the states and url flag counts.
 * <p/>
 * I'm very aware there are unchecked calls in this java file. It is currently a workaround for jenkins and sonar to
 * work with me, please bear with the IDE warnings
 */

public class MainWindow implements StatisticObserver {
    public static final String WAITING_FOR_DATA = "Waiting for data..";
    // accessors for the GUI components
    // Panels (Not all are referenced to in the code but it is needed for the UI to work.
    private JTabbedPane panelCrawlers;
    private JPanel mainPanel;
    private JPanel panelFlags;
    private JPanel panelCrawlerStatus;
    private JPanel panelTools;
    private JPanel paneCrawlers;
    // Textfields
    private JTextField tfUrlTotalVisited;
    private JTextField tfUrlTotalRetry;
    private JTextField tfUrlTotalFound;
    private JTextField tfUrlTotalFile;
    private JTextField tfUrlTotalDead;
    private JTextField tfUrlTotalRecrawl;
    private JTextField tfCrawlerTotalRunning;
    private JTextField tfTotalPaused;
    private JTextField tfTotalStopped;
    // Buttons
    private JButton btPauseAll;
    private JButton btResumeAll;
    private JButton btParser;
    private JButton btOpenSpecificCrawler;
    // ScrollPanes, used for containing lists and making them scrollable
    private JScrollPane listScrollPane;
    // Other components
    private JList listCrawlers;
    // Instance variables
    private Map<Flag, JTextField> totalUrlTextfieldMap;
    private Map<CrawlmanagerState, JTextField> totalCrawlerTextfieldMap;
    private Map<CrawlManager, CrawlmanagerState> stateMap
            = new HashMap<CrawlManager, CrawlmanagerState>();
    private DefaultListModel listModel;
    private Map<CrawlManager, Map<Flag, Integer>> flagMap
            = new HashMap<CrawlManager, Map<Flag, Integer>>();

    /**
     * Constructor of the Main window
     */
    private MainWindow() {
        // Register as observer to the Statistics Tracker.
        StatisticsTracker.registerObserver(this);
        // Sets default values for the data maps
        initializeCrawlerMap();
        initializeUrlMap();
        // Add listeners to the UI buttons
        addButtonListeners();

        initializeListModel();
    }

    public static MainWindow createMainWindow() {
        JFrame frame = new JFrame("JobHearted Crawl Application");
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        return new MainWindow();
    }

    /**
     * Initializes the ListModel, used for the data displayed in the list which is i nthe Specific info panel.
     */
    private void initializeListModel() {
        listModel = new DefaultListModel();
        listCrawlers.setModel(listModel);
        // We only want to be able to select one item in the list
        listCrawlers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    /**
     * Adds the listeners of the UI buttons
     */
    private void addButtonListeners() {
        btPauseAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == btPauseAll) {
                    setStateOfAllCrawlers(CrawlmanagerState.PAUSING);
                }
            }
        });

        btResumeAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == btResumeAll) {
                    setStateOfAllCrawlers(CrawlmanagerState.RUNNING);
                }

            }
        });

        btParser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == btParser) {
                    ParseUi.createparseUi();
                }
            }
        });

        btOpenSpecificCrawler.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == btOpenSpecificCrawler) {
                    openSelectedCrawler();
                }
            }
        });
    }

    /**
     * Opens a new window for the Crawler selected in the list
     */
    private void openSelectedCrawler() {
        int selected = listCrawlers.getSelectedIndex();
        if(selected != -1){
            // TODO: Add the window
        }
    }

    /**
     * Pushes the specified state to All crawl managers registered to the statistics tracker, and are thus in our
     * stateMap
     * @param newState State to push
     */
    private void setStateOfAllCrawlers(CrawlmanagerState newState) {
        for (Map.Entry<CrawlManager, CrawlmanagerState> entry : stateMap.entrySet()) {
            entry.getKey().setState(newState);
        }
    }

    /**
     * Fills the urlMap with null data, and set the text fields to a waiting message,
     * to indicate the program is still loading
     */
    private void initializeUrlMap() {
        totalUrlTextfieldMap = new HashMap<Flag, JTextField>();

        totalUrlTextfieldMap.put(Flag.FOUND, tfUrlTotalFound);
        totalUrlTextfieldMap.put(Flag.FILE, tfUrlTotalFile);
        totalUrlTextfieldMap.put(Flag.DEAD, tfUrlTotalDead);
        totalUrlTextfieldMap.put(Flag.RECRAWL, tfUrlTotalRecrawl);
        totalUrlTextfieldMap.put(Flag.VISITED, tfUrlTotalVisited);
        totalUrlTextfieldMap.put(Flag.RETRY, tfUrlTotalRetry);

        for (Map.Entry<Flag, JTextField> entry : totalUrlTextfieldMap.entrySet()) {
            entry.getValue().setText(WAITING_FOR_DATA);
        }
    }

    /**
     * Fills the crawlerMap with null data, and set the text fields to a waiting message,
     * to indicate the program is still loading
     */
    private void initializeCrawlerMap() {
        totalCrawlerTextfieldMap = new HashMap<CrawlmanagerState, JTextField>();

        totalCrawlerTextfieldMap.put(CrawlmanagerState.RUNNING, tfCrawlerTotalRunning);
        totalCrawlerTextfieldMap.put(CrawlmanagerState.PAUSED, tfTotalPaused);
        totalCrawlerTextfieldMap.put(CrawlmanagerState.STOPPED, tfTotalStopped);

        for (Map.Entry<CrawlmanagerState, JTextField> entry : totalCrawlerTextfieldMap.entrySet()) {
            if (entry.getValue() != null) entry.getValue().setText(WAITING_FOR_DATA);
        }
    }

    /**
     * this method is called when the observable StatisticsTracker has new information about an url for us.
     * Updated the info and recalculates the total for that flag.
     * @param crawlManager CrawlManager of which and url's flag was changed
     * @param flag  The flag which has new information
     * @param newCount New count of the urls which has that flag
     */
    @Override
    public void updateFlag(CrawlManager crawlManager, Flag flag, int newCount) {
        // Check if the crawlmanager is in the flagMap
        if (flagMap.get(crawlManager) == null) {
            flagMap.put(crawlManager, createNewFlagMap());
        }
        // put in the new count
        flagMap.get(crawlManager).put(flag, newCount);
        // update the text field of that flag
        totalUrlTextfieldMap.get(flag).setText(Integer.toString(getCountForFlag(flag)));
    }

    /**
     * Creates an Empty FlagMap
     * @return created flagmap
     */
    private Map<Flag,Integer> createNewFlagMap() {
        Map<Flag, Integer> flagIntegerMap = new HashMap<Flag, Integer>();
        for (Flag f : Flag.values()) {
            flagIntegerMap.put(f, 0);
        }

        return flagIntegerMap;
    }

    /**
     * Observer method for the GUI. When a crawlManager is updated, this method is called. The GUI is then updated
     * with the newly received information.
     *
     * @param crawlManager Crawlmanager to update state of
     * @param newState     New state of the CrawlManager
     */
    @Override
    public void updateCrawlerState(CrawlManager crawlManager, CrawlmanagerState newState) {
        stateMap.put(crawlManager, newState);

        // Add the Manager to the ListModel of the GUI, making it selectable in the list
        if (!listModel.contains(crawlManager)) {
            listModel.addElement(crawlManager);
        }

        // Update the general info panel in the GUI
        updateGeneralManagerInformation();
    }

    /**
     * Updates the general information panel of the GUI. Only the managers, not the url count
     */
    private void updateGeneralManagerInformation() {
        for (Map.Entry<CrawlmanagerState, JTextField> entry : totalCrawlerTextfieldMap.entrySet()) {
            entry.getValue().setText(Integer.toString(getCountForState(entry.getKey())));
        }
    }

    /**
     * This method returns the total count of all CrawlManagers that have a certain state. Used for updating the GUI
     *
     * @param state State to query count of
     * @return Count of crawlmanagers in this state
     */
    private int getCountForState(CrawlmanagerState state) {
        int count = 0;
        for (Map.Entry<CrawlManager, CrawlmanagerState> entry1 : stateMap.entrySet()) {
            if (entry1.getValue() == state) {
                count += 1;
            }
        }
        return count;
    }

    /**
     * This method returns the total count of all URLs that have a certain flag. Used for updating the GUI
     * @param flag Flag to query count of
     * @return Count of URLS with this flag
     */
    private int getCountForFlag(Flag flag) {
        int total = 0;
        for (Map.Entry<CrawlManager, Map<Flag, Integer>> entry : flagMap.entrySet()) {
            total += entry.getValue().get(flag);
        }

        return total;
    }

    /**
     * This method is called when a crawlManager is removed.
     *
     * @param crawlManager CrawlManager that was removed
     */
    @Override
    public void crawlerRemoved(CrawlManager crawlManager) {
        if (crawlManager != null) {
            // Remove all traces of the data
            stateMap.remove(crawlManager);
            listModel.removeElement(crawlManager);
            // Update the GUI
            updateGeneralManagerInformation();
        }
    }
}
