package org.jobhearted.crawler.gui;

import org.jobhearted.crawler.management.CrawlManager;
import org.jobhearted.crawler.management.CrawlmanagerState;
import org.jobhearted.crawler.objects.Flag;
import org.jobhearted.crawler.observers.StatisticObserver;
import org.jobhearted.crawler.parser.ParseUi;
import org.jobhearted.crawler.statistics.StatisticsTracker;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/12/13
 * Time: 9:29 PM
 */
@SuppressWarnings("BoundFieldAssignment")
public class MainWindow implements StatisticObserver {
    private JTabbedPane panelCrawlers;
    private JPanel mainPanel;
    private JPanel panelFlags;
    private JPanel panelCrawlerStatus;
    private JTextField tfUrlTotalVisited;
    private JTextField tfUrlTotalRetry;
    private JTextField tfUrlTotalFound;
    private JTextField tfUrlTotalFile;
    private JTextField tfUrlTotalDead;
    private JTextField tfUrlTotalRecrawl;
    private JLabel Recrawl;
    private JTextField tfCrawlerTotalRunning;
    private JTextField tfTotalPaused;
    private JTextField tfTotalStopped;
    private JButton tfButtonPauseAll;
    private JButton btResumeAll;
    private JPanel panelTools;
    private JButton btParser;
    private JPanel paneCrawlers;
    private JScrollPane listScrollPane;
    private JList<CrawlManager> listCrawlers;
    private JButton btOpenSpecificCrawler;

    private Map<Flag, JTextField> totalUrlTextfieldMap;
    private static Map<CrawlManager, Map<Flag, Integer>> flagMap = new HashMap<CrawlManager, Map<Flag, Integer>>();
    private Map<CrawlmanagerState, JTextField> totalCrawlerTextfieldMap;
    private Map<CrawlManager, CrawlmanagerState> stateMap;
    private DefaultListModel<CrawlManager> listModel;

    private MainWindow() {
        StatisticsTracker.registerObserver(this);
        initializeCrawlerMap();
        initializeUrlMap();
        flagMap = new HashMap<CrawlManager, Map<Flag, Integer>>();
        tfButtonPauseAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(Map.Entry<CrawlManager, CrawlmanagerState> entry : stateMap.entrySet()) {
                    entry.getKey().setState(CrawlmanagerState.PAUSING);
                }

            }
        });

        btResumeAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(Map.Entry<CrawlManager, CrawlmanagerState> entry : stateMap.entrySet()) {
                    entry.getKey().setState(CrawlmanagerState.RUNNING);
                }

            }
        });

        btParser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == btParser) {
                    ParseUi.createparseUi();
                }
            }
        });

        btOpenSpecificCrawler.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == btOpenSpecificCrawler) {
                    openSelectedCrawler();
                }
            }
        });

        listModel = new DefaultListModel<CrawlManager>();
        listCrawlers.setModel(listModel);
        listCrawlers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    }

    private void openSelectedCrawler() {
        int selected = listCrawlers.getSelectedIndex();
        System.out.println("Clicked crawler " + listModel.getElementAt(selected).toString());
    }

    private void initializeUrlMap() {
        totalUrlTextfieldMap = new HashMap<Flag, JTextField>();

        totalUrlTextfieldMap.put(Flag.FOUND, tfUrlTotalFound);
        totalUrlTextfieldMap.put(Flag.FILE, tfUrlTotalFile);
        totalUrlTextfieldMap.put(Flag.DEAD, tfUrlTotalDead);
        totalUrlTextfieldMap.put(Flag.RECRAWL, tfUrlTotalRecrawl);
        totalUrlTextfieldMap.put(Flag.VISITED, tfUrlTotalVisited);
        totalUrlTextfieldMap.put(Flag.RETRY, tfUrlTotalRetry);

        for(Map.Entry<Flag, JTextField> entry : totalUrlTextfieldMap.entrySet()) {
            entry.getValue().setText("Waiting for data..");
        }
    }

    private void initializeCrawlerMap() {
        totalCrawlerTextfieldMap = new HashMap<CrawlmanagerState, JTextField>();

        totalCrawlerTextfieldMap.put(CrawlmanagerState.RUNNING, tfCrawlerTotalRunning);
        totalCrawlerTextfieldMap.put(CrawlmanagerState.PAUSED, tfTotalPaused);
        totalCrawlerTextfieldMap.put(CrawlmanagerState.STOPPED, tfTotalStopped);

        for(Map.Entry<CrawlmanagerState, JTextField> entry : totalCrawlerTextfieldMap.entrySet()) {
            if(entry.getValue() != null) entry.getValue().setText("Waiting for data..");
        }
    }


    public static MainWindow createMainWindow() {
        JFrame frame = new JFrame("JobHearted Crawl Application");
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        return new MainWindow();
    }

    @Override
    public void updateFlag(CrawlManager crawlManager, Flag flag, int newCount) {
        if(flagMap.get(crawlManager) == null) {
            Map<Flag, Integer> flagIntegerMap = new HashMap<Flag, Integer>();
            for(Flag f : Flag.values()) {
                flagIntegerMap.put(f, 0);
            }
            flagMap.put(crawlManager, flagIntegerMap);
        }

        flagMap.get(crawlManager).put(flag, newCount);

        int total = 0;
        for(Map.Entry<CrawlManager, Map<Flag, Integer>> entry : flagMap.entrySet()) {
            total += entry.getValue().get(flag);
        }
        totalUrlTextfieldMap.get(flag).setText(Integer.toString(total));
    }

    @Override
    public void updateCrawlerState(CrawlManager crawlManager, CrawlmanagerState newState) {
        if(stateMap == null) {
            stateMap = new HashMap<CrawlManager, CrawlmanagerState>();
        }

        stateMap.put(crawlManager, newState);

        if(!listModel.contains(crawlManager)) {
            listModel.addElement(crawlManager);
        }


        for(Map.Entry<CrawlmanagerState, JTextField> entry : totalCrawlerTextfieldMap.entrySet()) {
            int count = 0;
            for(Map.Entry<CrawlManager, CrawlmanagerState> entry1 : stateMap.entrySet()) {
                if(entry1.getValue() == entry.getKey()) {
                    count += 1;
                }
            }

            entry.getValue().setText(Integer.toString(count));
        }


    }

    @Override
    public void crawlerRemoved(CrawlManager crawlManager) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
