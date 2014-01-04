package org.jobhearted.crawler.gui;


import org.jobhearted.crawler.management.Settings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Bound class for the settings window class. Reads and stores information into the Settings class by using
 * GUI components.
 * This is a singleton class. Only one instance can be active at a time. If the create method is called when the window
 * still exists, it requests focus instead.
 *
 * @see Settings
 */
public class SettingsWindow {
    // Window field for the singleton pattern
    private static JFrame WINDOW;

    // GUI elements
    private JTabbedPane settingsPane;
    private JPanel generalSettingsPanel;
    private JSpinner spCrawlTimeout;
    private JSpinner spRetryPolicy;
    private JSpinner spDbSaveInterval;
    private JButton btGeneralSave;
    private JSpinner spRecrawlInterval;
    private JPanel databaseSettingsPanel;
    private JTextField tfDbAddress;
    private JTextField tfDbPort;
    private JTextField tfDbUser;
    private JPasswordField pfDbPassword;
    private JButton btDbTest;
    private JButton btDbSave;
    private JTextField tfDbName;
    private JPanel settingsPanel;
    private JSpinner spRecrawlCheckTime;

    /**
     * private constructor for the SettingsWindow class.
     */
    private SettingsWindow() {
        loadData();
        createListeners();
    }

    /**
     * Returns the SettingsWindow, will create one if there is none.
     */
    public static synchronized void getSettingsWindow() {
        if (WINDOW == null) {
            WINDOW = new JFrame("Settings");
            WINDOW.setContentPane(new SettingsWindow().settingsPanel);
            WINDOW.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            WINDOW.pack();
        }
        WINDOW.setVisible(true);
        WINDOW.requestFocus();
    }

    /**
     * Adds button functionality to the GUI
     */
    private void createListeners() {
        btGeneralSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveGeneralData();
            }
        });
    }

    /**
     * Loads the Data for the GUI from the Settings class.
     */
    private void loadData() {
        //load general data
        Settings.loadSettings();

        this.spCrawlTimeout.setValue(Settings.CRAWL_TIMEOUT / 1000);
        this.spRetryPolicy.setValue(Settings.RETRY_POLICY);
        this.spRecrawlInterval.setValue(Settings.RECRAWL_TIME / 3600000);
        this.spRecrawlCheckTime.setValue(Settings.RECRAWL_CHECK_TIME / 60000);
    }

    /**
     * Saves the data currently in the GUI to the Settings class
     */
    private void saveGeneralData() {
        Settings.CRAWL_TIMEOUT = Integer.parseInt(spCrawlTimeout.getValue().toString()) * 1000;
        Settings.RETRY_POLICY = Integer.parseInt(spRetryPolicy.getValue().toString());
        Settings.RECRAWL_TIME = Integer.parseInt(spRecrawlInterval.getValue().toString()) * 3600000;
        Settings.RECRAWL_CHECK_TIME = Integer.parseInt(spRecrawlCheckTime.getValue().toString()) * 60000;

        Settings.saveSettings();
    }
}
