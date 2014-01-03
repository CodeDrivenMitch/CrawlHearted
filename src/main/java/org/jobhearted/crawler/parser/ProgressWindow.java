package org.jobhearted.crawler.parser;

import javax.swing.*;

/**
 * This is the Bound Class for the ProgressWindow UI form. This window is utilized for multiple goals
 */
public class ProgressWindow {
    // UI components
    private JProgressBar progressBar;
    private JPanel panelProgress;
    private int maxProgress;
    private JFrame frame;

    /**
     * Constructor for the progressWindow. Declared private because of the use of a Factory Method.
     * @param maxProgress Maximum progress integer.
     */
    private ProgressWindow(int maxProgress) {
        this.maxProgress = maxProgress;
        this.progressBar.setMaximum(maxProgress);
        this.progressBar.setValue(0);
    }

    /**
     * Sets the new current progress value of the window, updating the progress bar.
     * @param value new current progress integer
     */
    public void setNewProgressValue(int value) {
        this.progressBar.setValue(value);

        if(value == maxProgress -1) {
            frame.setVisible(false);
        }
    }

    /**
     * Sets the frame instance variable. Used for the window to be able to close itself when progress reaches 100%.
     * @param frame Frame to set the field to
     */
    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    /**
     * Factory method for creating a progress window. Also makes it visible, makes sure it cannot be closed and sets
     * the frame instance variable of the window.
     * @param maxProgress Maximum progress integer
     * @return The window now shown.
     */
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
