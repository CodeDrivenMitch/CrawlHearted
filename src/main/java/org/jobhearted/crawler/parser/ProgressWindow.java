package org.jobhearted.crawler.parser;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/15/13
 * Time: 1:23 PM
 */
public class ProgressWindow {
    private JProgressBar progressBar;
    private JPanel panelProgress;
    private int maxProgress;
    private int currentProgress;
    private JFrame frame;

    private ProgressWindow(int maxProgress) {
        this.maxProgress = maxProgress;
        this.progressBar.setMaximum(maxProgress);
        this.progressBar.setValue(0);
    }

    public void setNewProgressValue(int value) {
        this.progressBar.setValue(value);
        this.currentProgress = value;

        if(currentProgress == maxProgress -1) {
            frame.setVisible(false);
        }
    }


    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public static ProgressWindow createProgressWindow(int maxProgress) {
        ProgressWindow window = new ProgressWindow(maxProgress);
       JFrame frame = new JFrame("Bezig...");
        frame.setContentPane(window.panelProgress);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        window.setFrame(frame);
        return window;
    }
}
